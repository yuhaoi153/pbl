package flyfish.service.impl;

import flyfish.mapper.M_CheckSemesterMapper;
import flyfish.pojo.DTO.M_UpdateSemesterDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.Year;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class M_CheckSemesterServiceImplTest {

    private M_CheckSemesterMapper mapper;
    private M_CheckSemesterServiceImpl service;

    @BeforeEach
    void setUp() {
        mapper = mock(M_CheckSemesterMapper.class);
        service = new M_CheckSemesterServiceImpl(mapper);
        when(mapper.countGradeYearByYear(Year.now().getValue())).thenReturn(1);
        when(mapper.markSemesterUpdateSucceeded(anyString(), anyString(), anyString()))
                .thenReturn(1);
    }

    @Test
    void updatesHomeworkMessagesInPrimaryKeyBatches() {
        M_UpdateSemesterDTO request = new M_UpdateSemesterDTO(
                null, "附小", "chat", "homeworkMessage", null);
        when(mapper.getDataBaseTypeByTableName("chat", "homeworkMessage"))
                .thenReturn("第一种");
        List<Integer> firstBatch = List.of(1, 1200);
        List<Integer> secondBatch = List.of(1300, 2500);
        when(mapper.getHomeworkMessageBatchIds("附小", 0, 3000)).thenReturn(firstBatch);
        when(mapper.getHomeworkMessageBatchIds("附小", 1200, 3000)).thenReturn(secondBatch);
        when(mapper.getHomeworkMessageBatchIds("附小", 2500, 3000)).thenReturn(List.of());
        when(mapper.updateHomeworkMessageClassNameBatch("附小", firstBatch)).thenReturn(1000);
        when(mapper.updateHomeworkMessageClassNameBatch("附小", secondBatch)).thenReturn(350);

        List<String> result = service.updateSemester(List.of(request));

        assertThat(result).containsExactly("chat.homeworkMessage：更新完成，共更新 1350 条数据");
        var order = inOrder(mapper);
        order.verify(mapper).getHomeworkMessageBatchIds("附小", 0, 3000);
        order.verify(mapper).updateHomeworkMessageClassNameBatch("附小", firstBatch);
        order.verify(mapper).getHomeworkMessageBatchIds("附小", 1200, 3000);
        order.verify(mapper).updateHomeworkMessageClassNameBatch("附小", secondBatch);
        order.verify(mapper).getHomeworkMessageBatchIds("附小", 2500, 3000);
        order.verify(mapper).markSemesterUpdateSucceeded("chat", "homeworkMessage", "附小");
    }

    @Test
    void insertsTrackingRowWhenItDoesNotExist() {
        M_UpdateSemesterDTO request = new M_UpdateSemesterDTO(
                null, "附小", "chat", "homeworkMessage", null);
        when(mapper.getDataBaseTypeByTableName("chat", "homeworkMessage"))
                .thenReturn("第一种");
        when(mapper.markSemesterUpdateSucceeded("chat", "homeworkMessage", "附小"))
                .thenReturn(0);

        service.updateSemester(List.of(request));

        verify(mapper).markSemesterUpdateSucceeded("chat", "homeworkMessage", "附小");
        verify(mapper).insertSemesterUpdateSucceeded("chat", "homeworkMessage", "附小");
    }

    @Test
    void rejectsIncompleteRequestWithoutTouchingDatabase() {
        List<String> result = service.updateSemester(List.of(
                new M_UpdateSemesterDTO(null, " ", "chat", "homeworkMessage", null)));

        assertThat(result).containsExactly("更新失败：school、dataBaseName、tableName 不能为空");
    }

    @Test
    void skipsTableThatHasAlreadyBeenUpdated() {
        M_UpdateSemesterDTO request = new M_UpdateSemesterDTO(
                null, "附小", "chat", "homeworkMessage", null);
        when(mapper.getDataBaseTypeByTableName("chat", "homeworkMessage"))
                .thenReturn("第一种");
        when(mapper.getUpdateStatusByTableName("chat", "homeworkMessage", "附小"))
                .thenReturn("1");

        List<String> result = service.updateSemester(List.of(request));

        assertThat(result).containsExactly("chat.homeworkMessage：已经更新过，本次已跳过");
    }

    @Test
    void updatesSecondTypeTableInPrimaryKeyBatches() {
        M_UpdateSemesterDTO request = new M_UpdateSemesterDTO(
                null, "附小", "miniprograme", "poorPerformer", null);
        when(mapper.getDataBaseTypeByTableName("miniprograme", "poorPerformer"))
                .thenReturn("第二种");
        List<Integer> firstBatch = List.of(2, 3005);
        List<Integer> secondBatch = List.of(3010, 4100);
        when(mapper.getClassNameAndGradeBatchIds(
                "miniprograme", "poorPerformer", "附小", 0, 3000)).thenReturn(firstBatch);
        when(mapper.getClassNameAndGradeBatchIds(
                "miniprograme", "poorPerformer", "附小", 3005, 3000)).thenReturn(secondBatch);
        when(mapper.getClassNameAndGradeBatchIds(
                "miniprograme", "poorPerformer", "附小", 4100, 3000)).thenReturn(List.of());
        when(mapper.updateClassNameAndGradeBatch(
                "miniprograme", "poorPerformer", "附小", firstBatch)).thenReturn(3000);
        when(mapper.updateClassNameAndGradeBatch(
                "miniprograme", "poorPerformer", "附小", secondBatch)).thenReturn(500);

        List<String> result = service.updateSemester(List.of(request));

        assertThat(result).containsExactly(
                "miniprograme.poorPerformer：更新完成，共更新 3500 条数据");
        verify(mapper).markSemesterUpdateSucceeded(
                "miniprograme", "poorPerformer", "附小");
    }

    @Test
    void refreshesGradeYearAndInsertsCurrentYearWhenMissing() {
        int currentYear = Year.now().getValue();
        when(mapper.countGradeYearByYear(currentYear)).thenReturn(0);

        List<String> result = service.updateSemester(List.of());

        var order = inOrder(mapper);
        order.verify(mapper).countGradeYearByYear(currentYear);
        order.verify(mapper).refreshGradeYearGrades(currentYear);
        order.verify(mapper).insertCurrentGradeYear(currentYear);
        assertThat(result).containsExactly("没有需要更新的数据");
    }

    @ParameterizedTest
    @CsvSource({
            "第三种, accumulateScore, classNumber",
            "第四种, feedbackConstant, class_number",
            "第五种, giftRedemption, className",
            "第六种, user, username"
    })
    void routesNumericClassTypesToTheirConfiguredColumn(
            String dataBaseType, String tableName, String columnName) {
        M_UpdateSemesterDTO request = new M_UpdateSemesterDTO(
                null, "附小", "homework", tableName, null);
        when(mapper.getDataBaseTypeByTableName("homework", tableName))
                .thenReturn(dataBaseType);
        when(mapper.getNumericClassBatchIds(
                "homework", tableName, columnName, "附小", 0L, 3000))
                .thenReturn(List.of());

        List<String> result = service.updateSemester(List.of(request));

        assertThat(result).containsExactly(
                "homework." + tableName + "：更新完成，共更新 0 条数据");
        verify(mapper).getNumericClassBatchIds(
                "homework", tableName, columnName, "附小", 0L, 3000);
        verify(mapper).markSemesterUpdateSucceeded("homework", tableName, "附小");
    }

    @Test
    void updatesSeventhTypeGradeFieldInBatches() {
        M_UpdateSemesterDTO request = new M_UpdateSemesterDTO(
                null, "附小", "miniprograme", "examination", null);
        when(mapper.getDataBaseTypeByTableName("miniprograme", "examination"))
                .thenReturn("第七种");
        List<Long> ids = List.of(10L, 3500L);
        when(mapper.getGradeBatchIds(
                "miniprograme", "examination", "附小", 0L, 3000)).thenReturn(ids);
        when(mapper.getGradeBatchIds(
                "miniprograme", "examination", "附小", 3500L, 3000)).thenReturn(List.of());
        when(mapper.updateGradeBatch(
                "miniprograme", "examination", "附小", ids)).thenReturn(2);

        List<String> result = service.updateSemester(List.of(request));

        assertThat(result).containsExactly(
                "miniprograme.examination：更新完成，共更新 2 条数据");
    }

    @Test
    void updatesEighthTypeGradeAndClassNameTogetherInBatches() {
        M_UpdateSemesterDTO request = new M_UpdateSemesterDTO(
                null, "附小", "miniprograme", "grade_classname", null);
        when(mapper.getDataBaseTypeByTableName("miniprograme", "grade_classname"))
                .thenReturn("第八种");
        List<Long> ids = List.of(1L, 2L);
        when(mapper.getGradeAndClassNameBatchIds(
                "miniprograme", "grade_classname", "附小", 0L, 3000)).thenReturn(ids);
        when(mapper.getGradeAndClassNameBatchIds(
                "miniprograme", "grade_classname", "附小", 2L, 3000)).thenReturn(List.of());
        when(mapper.updateGradeAndClassNameBatch(
                "miniprograme", "grade_classname", "附小", ids)).thenReturn(2);

        List<String> result = service.updateSemester(List.of(request));

        assertThat(result).containsExactly(
                "miniprograme.grade_classname：更新完成，共更新 2 条数据");
    }
}
