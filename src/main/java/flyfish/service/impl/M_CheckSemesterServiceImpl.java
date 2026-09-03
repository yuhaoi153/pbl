package flyfish.service.impl;

import flyfish.mapper.M_CheckSemesterMapper;
import flyfish.pojo.DTO.M_UpdateSemesterDTO;
import flyfish.pojo.VO.M_UpdateTableVO;

import flyfish.service.M_CheckSemesterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

@Service
@Slf4j
public class M_CheckSemesterServiceImpl implements M_CheckSemesterService {

    private static final int UPDATE_BATCH_SIZE = 3000;
    private static final String HOMEWORK_MESSAGE_TABLE = "chat.homeworkMessage";
    private static final Pattern SQL_IDENTIFIER_PATTERN = Pattern.compile("[A-Za-z0-9_]+");
    private final Set<String> runningTasks = ConcurrentHashMap.newKeySet();
    private final Object gradeYearUpdateLock = new Object();
    private final M_CheckSemesterMapper mCheckSemesterMapper;

    public M_CheckSemesterServiceImpl(M_CheckSemesterMapper mCheckSemesterMapper) {
        this.mCheckSemesterMapper = mCheckSemesterMapper;
    }

    /**
     * 查询三个数据库中该学校分别有多少个table，找出其中有year字段的table，同时查询是不是已经更新过
     * @param school
     * @return
     */
    @Override
    public List<M_UpdateTableVO> getAllTable(String school) {
        List<M_UpdateTableVO> updateTableVOList = mCheckSemesterMapper.getAllTable(school);
        if (updateTableVOList != null && !updateTableVOList.isEmpty()) {
            for(M_UpdateTableVO updateTableVO : updateTableVOList) {
                //查询该表是否已经更新过
                String tableName = updateTableVO.getTableName();
                String dataBaseName = updateTableVO.getDataBaseName();
                //查询该表是否已经更新过
                Integer count = mCheckSemesterMapper.getCountByTableName(dataBaseName, tableName,school);
                String updateStatus = mCheckSemesterMapper.getUpdateStatusByTableName(dataBaseName, tableName,school);
                if(updateStatus != null && updateStatus.equals("1")) {
                    updateTableVO.setUpdateStatus("已更新");
                } else {
                    updateTableVO.setUpdateStatus("未更新");
                }

                if(count != null && count > 0) {
                    updateTableVO.setUpdateCount(count);
                } else {
                    updateTableVO.setUpdateCount(0);}

            }
        }
        return updateTableVOList;
    }

    /**
     * 更新年级，并记录到数据库中
     * @param updateSemesterDTOList
     * @return
     */
    @Override
    public List<String> updateSemester(List<M_UpdateSemesterDTO> updateSemesterDTOList) {
        ensureCurrentGradeYear();

        List<String> responses = new ArrayList<>();
        if (updateSemesterDTOList == null || updateSemesterDTOList.isEmpty()) {
            responses.add("没有需要更新的数据");
            return responses;
        }

        for(M_UpdateSemesterDTO updateSemesterDTO : updateSemesterDTOList) {
            if (updateSemesterDTO == null
                    || isBlank(updateSemesterDTO.getSchool())
                    || isBlank(updateSemesterDTO.getDataBaseName())
                    || isBlank(updateSemesterDTO.getTableName())) {
                responses.add("更新失败：school、dataBaseName、tableName 不能为空");
                continue;
            }

            String dataBaseName = updateSemesterDTO.getDataBaseName().trim();
            String tableName = updateSemesterDTO.getTableName();
            String school = updateSemesterDTO.getSchool().trim();
            String checkTableName = dataBaseName + "." + tableName.trim();
            String dataBaseType = mCheckSemesterMapper.getDataBaseTypeByTableName(dataBaseName, tableName.trim());
            if (dataBaseType == null) {
                throw new IllegalArgumentException("无法获取表的数据库类型，请检查表名是否正确：" + checkTableName);
            }else {
                updateSemesterDTO.setDataBaseType(dataBaseType);
            }
            switch (dataBaseType) {
                case "第一种":
                    responses.add(updateHomeworkMessageClassName(school, dataBaseName, tableName.trim()));
                    break;
                case "第二种":
                    responses.add(updateClassNameAndGrade(
                            school, dataBaseName, tableName.trim()));
                    break;
                case "第三种":
                    responses.add(updateNumericClassField(
                            school, dataBaseName, tableName.trim(), "classNumber"));
                    break;
                case "第四种":
                    responses.add(updateNumericClassField(
                            school, dataBaseName, tableName.trim(), "class_number"));
                    break;
                case "第五种":
                    responses.add(updateNumericClassField(
                            school, dataBaseName, tableName.trim(), "className"));
                    break;
                case "第六种":
                    responses.add(updateNumericClassField(
                            school, dataBaseName, tableName.trim(), "username"));
                    break;
                case "第七种":
                    responses.add(updateGradeField(
                            school, dataBaseName, tableName.trim()));
                    break;
                case "第八种":
                    responses.add(updateGradeAndChineseClassName(
                            school, dataBaseName, tableName.trim()));
                    break;

                default:
                    responses.add(checkTableName + "：暂不支持更新");
                    break;
            }
        }
        return responses;
    }

    private String updateHomeworkMessageClassName(String school, String dataBaseName, String tableName) {
        String taskKey = school + ':' + dataBaseName + '.' + tableName;
        if (!runningTasks.add(taskKey)) {
            return HOMEWORK_MESSAGE_TABLE + "：该学校的年级更新正在执行，请勿重复提交";
        }

        try {
            if ("1".equals(mCheckSemesterMapper.getUpdateStatusByTableName(
                    dataBaseName, tableName, school))) {
                return HOMEWORK_MESSAGE_TABLE + "：已经更新过，本次已跳过";
            }
            int lastId = 0;
            long updatedTotal = 0;
            while (true) {
                List<Integer> batchIds = mCheckSemesterMapper.getHomeworkMessageBatchIds(
                        school, lastId, UPDATE_BATCH_SIZE);
                if (batchIds == null || batchIds.isEmpty()) {
                    break;
                }

                updatedTotal += mCheckSemesterMapper.updateHomeworkMessageClassNameBatch(
                        school, batchIds);
                lastId = batchIds.get(batchIds.size() - 1);
            }

            recordSemesterUpdateSucceeded(dataBaseName, tableName, school);
            log.info("年级更新完成：table={}, school={}, updatedRows={}",
                    HOMEWORK_MESSAGE_TABLE, school, updatedTotal);
            return HOMEWORK_MESSAGE_TABLE + "：更新完成，共更新 " + updatedTotal + " 条数据";
        } catch (RuntimeException exception) {
            log.error("年级更新失败：table={}, school={}", HOMEWORK_MESSAGE_TABLE, school, exception);
            return HOMEWORK_MESSAGE_TABLE + "：更新失败，" + exception.getMessage();
        } finally {
            runningTasks.remove(taskKey);
        }
    }

    private String updateClassNameAndGrade(String school, String dataBaseName, String tableName) {
        String qualifiedTableName = dataBaseName + '.' + tableName;
        if (!isSafeSqlIdentifier(dataBaseName) || !isSafeSqlIdentifier(tableName)) {
            return qualifiedTableName + "：更新失败，库名或表名不合法";
        }

        String taskKey = school + ':' + qualifiedTableName;
        if (!runningTasks.add(taskKey)) {
            return qualifiedTableName + "：该学校的年级更新正在执行，请勿重复提交";
        }

        try {
            if ("1".equals(mCheckSemesterMapper.getUpdateStatusByTableName(
                    dataBaseName, tableName, school))) {
                return qualifiedTableName + "：已经更新过，本次已跳过";
            }
            int lastId = 0;
            long updatedTotal = 0;
            while (true) {
                List<Integer> batchIds = mCheckSemesterMapper.getClassNameAndGradeBatchIds(
                        dataBaseName, tableName, school, lastId, UPDATE_BATCH_SIZE);
                if (batchIds == null || batchIds.isEmpty()) {
                    break;
                }

                updatedTotal += mCheckSemesterMapper.updateClassNameAndGradeBatch(
                        dataBaseName, tableName, school, batchIds);
                lastId = batchIds.get(batchIds.size() - 1);
            }

            recordSemesterUpdateSucceeded(dataBaseName, tableName, school);
            log.info("年级更新完成：table={}, school={}, updatedRows={}",
                    qualifiedTableName, school, updatedTotal);
            return qualifiedTableName + "：更新完成，共更新 " + updatedTotal + " 条数据";
        } catch (RuntimeException exception) {
            log.error("年级更新失败：table={}, school={}", qualifiedTableName, school, exception);
            return qualifiedTableName + "：更新失败，" + exception.getMessage();
        } finally {
            runningTasks.remove(taskKey);
        }
    }

    private String updateNumericClassField(String school, String dataBaseName,
                                           String tableName, String columnName) {
        if (!isSafeSqlIdentifier(columnName)) {
            return dataBaseName + '.' + tableName + "：更新失败，字段名不合法";
        }
        return runLongIdBatchedUpdate(
                school,
                dataBaseName,
                tableName,
                (lastId, batchSize) -> mCheckSemesterMapper.getNumericClassBatchIds(
                        dataBaseName, tableName, columnName, school, lastId, batchSize),
                ids -> mCheckSemesterMapper.updateNumericClassBatch(
                        dataBaseName, tableName, columnName, school, ids));
    }

    private String updateGradeField(String school, String dataBaseName, String tableName) {
        return runLongIdBatchedUpdate(
                school,
                dataBaseName,
                tableName,
                (lastId, batchSize) -> mCheckSemesterMapper.getGradeBatchIds(
                        dataBaseName, tableName, school, lastId, batchSize),
                ids -> mCheckSemesterMapper.updateGradeBatch(
                        dataBaseName, tableName, school, ids));
    }

    private String updateGradeAndChineseClassName(String school, String dataBaseName,
                                                  String tableName) {
        return runLongIdBatchedUpdate(
                school,
                dataBaseName,
                tableName,
                (lastId, batchSize) -> mCheckSemesterMapper.getGradeAndClassNameBatchIds(
                        dataBaseName, tableName, school, lastId, batchSize),
                ids -> mCheckSemesterMapper.updateGradeAndClassNameBatch(
                        dataBaseName, tableName, school, ids));
    }

    private String runLongIdBatchedUpdate(String school, String dataBaseName, String tableName,
                                          LongBatchIdLoader idLoader, LongBatchUpdater updater) {
        String qualifiedTableName = dataBaseName + '.' + tableName;
        if (!isSafeSqlIdentifier(dataBaseName) || !isSafeSqlIdentifier(tableName)) {
            return qualifiedTableName + "：更新失败，库名或表名不合法";
        }

        String taskKey = school + ':' + qualifiedTableName;
        if (!runningTasks.add(taskKey)) {
            return qualifiedTableName + "：该学校的年级更新正在执行，请勿重复提交";
        }

        try {
            if ("1".equals(mCheckSemesterMapper.getUpdateStatusByTableName(
                    dataBaseName, tableName, school))) {
                return qualifiedTableName + "：已经更新过，本次已跳过";
            }
            long lastId = 0L;
            long updatedTotal = 0L;
            while (true) {
                List<Long> batchIds = idLoader.load(lastId, UPDATE_BATCH_SIZE);
                if (batchIds == null || batchIds.isEmpty()) {
                    break;
                }

                updatedTotal += updater.update(batchIds);
                lastId = batchIds.get(batchIds.size() - 1);
            }

            recordSemesterUpdateSucceeded(dataBaseName, tableName, school);
            log.info("年级更新完成：table={}, school={}, updatedRows={}",
                    qualifiedTableName, school, updatedTotal);
            return qualifiedTableName + "：更新完成，共更新 " + updatedTotal + " 条数据";
        } catch (RuntimeException exception) {
            log.error("年级更新失败：table={}, school={}", qualifiedTableName, school, exception);
            return qualifiedTableName + "：更新失败，" + exception.getMessage();
        } finally {
            runningTasks.remove(taskKey);
        }
    }

    private void recordSemesterUpdateSucceeded(String dataBaseName, String tableName, String school) {
        int affectedRows = mCheckSemesterMapper.markSemesterUpdateSucceeded(
                dataBaseName, tableName, school);
        if (affectedRows == 0) {
            mCheckSemesterMapper.insertSemesterUpdateSucceeded(dataBaseName, tableName, school);
        }
    }

    private void ensureCurrentGradeYear() {
        int currentYear = Year.now().getValue();
        synchronized (gradeYearUpdateLock) {
            if (mCheckSemesterMapper.countGradeYearByYear(currentYear) > 0) {
                return;
            }

            int updatedRows = mCheckSemesterMapper.refreshGradeYearGrades(currentYear);
            mCheckSemesterMapper.insertCurrentGradeYear(currentYear);
            log.info("grade_year 年级映射已更新：currentYear={}, updatedRows={}",
                    currentYear, updatedRows);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private boolean isSafeSqlIdentifier(String value) {
        return value != null && SQL_IDENTIFIER_PATTERN.matcher(value).matches();
    }

    @FunctionalInterface
    private interface LongBatchIdLoader {
        List<Long> load(long lastId, int batchSize);
    }

    @FunctionalInterface
    private interface LongBatchUpdater {
        int update(List<Long> ids);
    }
}
