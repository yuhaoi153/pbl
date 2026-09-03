package flyfish.service.impl;

import flyfish.mapper.*;
import flyfish.pojo.Menu;
import flyfish.pojo.PBLpojo.PBL_NameScoreDTO;
import flyfish.pojo.PBLpojo.PBL_QuestionItem;
import flyfish.pojo.PBLpojo.PBL_VoteDTO;
import flyfish.pojo.PBLpojo.PBL_VoteResult;
import flyfish.pojo.VO.M_BackGroundVO;
import flyfish.service.PBL_GeoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class PBL_GeoServiceImpl implements PBL_GeoService {
    @Autowired
    private PBL_GeoMapper pBL_GeoMapper;
    @Autowired
    private PBL_QuestionAnswerMapper pBL_QuestionAnswerMapper;
    @Autowired
    private PBL_QuestionItemMapper pBL_QuestionItemMapper;
    @Autowired
    private ChatMessageMapper  chatMessageMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private PBL_MenuMapper pBL_MenuMapper;
    @Autowired
    private PBL_ChatMessageMapper pblchatMessageMapper;

    @Override
    public List<Menu> confirmUser(String username, String password, String school) {
        String realPassword = pBL_GeoMapper.getPassword(username,school);
        if(realPassword.equals(password)){
            Integer userId = pBL_GeoMapper.getUserId(username,school);
            Integer roleId = pBL_GeoMapper.getRoleId(username,school);
            List<Menu> menuList = pBL_GeoMapper.getMenuByRoleId(roleId);
            for(Menu menu:menuList){
                menu.setUserId(userId);
            }
            return menuList;
        }else {
            throw new RuntimeException("用户名或密码错误");
        }
    }

    @Override
    public List<M_BackGroundVO> getImgUrl(String position, String school) {
        List<M_BackGroundVO> imageUlrList = pBL_GeoMapper.getImageUrlList(position,school);
        return imageUlrList;
    }

    @Override
    public String votefinalPbl(PBL_VoteDTO pBLVoteDTO) {
        //日期，学校，页名，课程名，学科，用户名，图片链接。    ，supplementray对应的所有人是谁对应的哪个题项，这个题项的所有人是谁。


        List<Integer> questionIdList = pBLVoteDTO.getQuestionIdList();
        pBL_QuestionAnswerMapper.deleteByUserName(pBLVoteDTO.getUsername(),pBLVoteDTO.getSchool(),pBLVoteDTO.getLessonName(),pBLVoteDTO.getPageName(), LocalDate.now());

        for (Integer questionId : questionIdList) {
            PBL_QuestionItem pblQuestionItem = pBL_QuestionItemMapper.getItemById(questionId);
            if(pblQuestionItem == null){
                throw new RuntimeException("题项不存在");
            }
            pBLVoteDTO.setQuestionId(questionId);
            pBLVoteDTO.setSupplementary(pblQuestionItem.getUsername());
            pBLVoteDTO.setCheckDate(LocalDate.now());
            pBLVoteDTO.setAnswerContent(pblQuestionItem.getContent());

            pBL_QuestionAnswerMapper.addItem(pBLVoteDTO);

        }


        return "最终作品投票成功";
    }

    @Override
    public String unlockMenu(String username, String school, String menuName) {
            Integer roleId = 10;
            pBL_MenuMapper.lockAll(roleId,school);
            pBL_MenuMapper.unlock(roleId,menuName,school);
            return "菜单解锁成功";

    }

    @Override
    public String getStatus(String menuName) {
        Integer roleId = 10;
        String status = pBL_MenuMapper.getStatus(roleId,menuName);
        return status;
    }

    @Override
    public void resetAllAnswer(String school, String lessonName, String subject, String pageName, LocalDate checkDate) {
        pBL_QuestionAnswerMapper.resetAllByCheckDate(checkDate);
        pageName = "最终作品";
        pBL_QuestionItemMapper.resetAllByCheckDate(pageName,checkDate);
        pblchatMessageMapper.resetAllByCheckDate(checkDate);
    }


    @Override
    public String votePbl(PBL_VoteDTO pBLVoteDTO) {

        pBL_QuestionAnswerMapper.addItem(pBLVoteDTO);
        return "投票成功";

    }

    @Override
    public List<PBL_QuestionItem> getQuestionInfo(String pageName, String school, String lessonName, String subject,LocalDate checkDate) {
        List<PBL_QuestionItem> questionItems = pBL_QuestionAnswerMapper.getQuestionInfo(pageName,school,lessonName,subject,checkDate);
        for (PBL_QuestionItem questionItem : questionItems) {
            questionItem.setQuestionId(questionItem.getId());
        }
        return questionItems;

    }

    @Override
    public List<PBL_VoteDTO> getVoteResult(String school, String lessonName, String subject, String pageName, String username, LocalDate checkDate) {
        List<PBL_VoteDTO> voteDTOList = pBL_QuestionAnswerMapper.getVoteResult(school, lessonName, subject, pageName, username,checkDate);
        return voteDTOList;
    }

    @Override
    public Map<String,PBL_VoteResult> getClassVoteResult(String school, String lessonName, String subject, String pageName,String voteA,String  voteB,String voteC,String voteD,LocalDate checkDate) {
        List<PBL_VoteDTO> voteDTOList = pBL_QuestionAnswerMapper.getVoteClassResult(school, lessonName, subject, pageName,checkDate);

        //按照voteDTO的content进行分类
        // 按照 content 字段进行分组，得到一个 Map<String, List<PBL_VoteDTO>>
        Map<String, List<PBL_VoteDTO>> groupedByContent = voteDTOList.stream()
                .collect(Collectors.groupingBy(PBL_VoteDTO::getQuestionName));
        Map<String,PBL_VoteResult> pblVoteResultMap = new HashMap<>();


        for(Map.Entry<String, List<PBL_VoteDTO>> entry : groupedByContent.entrySet()){
            List<String> voteAList = new ArrayList<String>();
            List<String> voteBList = new ArrayList<String>();
            List<String> voteCList = new ArrayList<String>();
            List<String> voteDList = new ArrayList<String>();
            for (PBL_VoteDTO voteDTO : entry.getValue()) {
                if (voteDTO.getAnswerContent().equals(voteA)) {
                    voteAList.add(voteDTO.getUsername());
                }else if (voteDTO.getAnswerContent().equals(voteB)) {
                    voteBList.add(voteDTO.getUsername());
                }else if (voteDTO.getAnswerContent().equals(voteC)) {
                    voteCList.add(voteDTO.getUsername());
                }else if (voteDTO.getAnswerContent().equals(voteD)) {
                    voteDList.add(voteDTO.getUsername());
                }
            }
            PBL_VoteResult pblVoteResult = new PBL_VoteResult();
            pblVoteResult.setVotersChooseA(voteAList);
            pblVoteResult.setVotersChooseB(voteBList);
            pblVoteResult.setVotersChooseC(voteCList);
            pblVoteResult.setVotersChooseD(voteDList);

            pblVoteResultMap.put(entry.getKey(),pblVoteResult);


        }
        return pblVoteResultMap;


    }

    @Override
    public String uploadFinalWork(PBL_VoteDTO pblVoteDTO) {
        PBL_QuestionItem pblQuestionItem = new PBL_QuestionItem();
        pblQuestionItem.setContent(pblVoteDTO.getAnswerContent());
        pblQuestionItem.setPageName(pblVoteDTO.getPageName());
        pblQuestionItem.setLessonName(pblVoteDTO.getLessonName());
        pblQuestionItem.setSchool(pblVoteDTO.getSchool());
        pblQuestionItem.setSubject("数学");
        pblQuestionItem.setUsername(pblVoteDTO.getUsername());
        pblQuestionItem.setCheckDate(LocalDate.now());


        pBL_QuestionItemMapper.deleteItem(pblQuestionItem);
        pBL_QuestionItemMapper.insertItem(pblQuestionItem);



        return "成功上传最终作品";
    }

    @Override
    public Map<Integer, Integer> getfinalWorkRank(String school, String lessonName, String subject, String pageName, LocalDate checkDate) {
        // 1. 获取今日所有投票记录
        List<PBL_VoteDTO> pblVoteDTOList = pBL_QuestionAnswerMapper.getQuestionAnswer(school, lessonName, subject, pageName, checkDate);

        // 2. 按 supplementary 计数票数
        Map<String, Integer> finalWorkRankScore = new HashMap<>();
        for (PBL_VoteDTO voteDTO : pblVoteDTOList) {
            String supplementary = voteDTO.getSupplementary();
            finalWorkRankScore.put(supplementary, finalWorkRankScore.getOrDefault(supplementary, 0) + 1);
        }

        // 3. 按票数降序排序
        List<Map.Entry<String, Integer>> sortedEntries = finalWorkRankScore.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toList());

        int total = sortedEntries.size();

        // 4. 计算奖级分界并收集各奖级 ID（用于后续赋分）
        List<Integer> top20Ids = new ArrayList<>();   // 前20% 一等奖
        List<Integer> mid30Ids = new ArrayList<>();   // 20%~50% 二等奖
        List<Integer> rest50Ids = new ArrayList<>();  // 剩余50% 三等奖

        if (total > 0) {
            int top20Count = (int) Math.ceil(total * 0.2);
            int top50Count = (int) Math.ceil(total * 0.5);

            int top20Index = top20Count - 1;
            int top50Index = top50Count - 1;

            // 处理并列：确保相同票数的作品不被拆分到不同奖级
            if (top20Index < total - 1) {
                int boundaryValue = sortedEntries.get(top20Index).getValue();
                while (top20Index + 1 < total && sortedEntries.get(top20Index + 1).getValue() == boundaryValue) {
                    top20Index++;
                }
            }
            if (top50Index < total - 1) {
                int boundaryValue = sortedEntries.get(top50Index).getValue();
                while (top50Index + 1 < total && sortedEntries.get(top50Index + 1).getValue() == boundaryValue) {
                    top50Index++;
                }
            }

            // 收集各奖级的 questionId
            for (int i = 0; i < total; i++) {
                String supplementary = sortedEntries.get(i).getKey();
                Integer questionId = pBL_QuestionAnswerMapper.getQuestionInfoBySupplementary(supplementary,checkDate);
                if (questionId == null) {
                    log.warn("无法根据 supplementary {} 获取 questionId", supplementary);
                    continue;
                }

                if (i <= top20Index) {
                    top20Ids.add(questionId);
                } else if (i <= top50Index) {
                    mid30Ids.add(questionId);
                } else {
                    rest50Ids.add(questionId);
                }
            }

            // TODO: 在此处执行赋分逻辑（最高15分，中间10分，最低5分）
            // 例如：
            updateScores(top20Ids,15);
            updateScores(mid30Ids,10);
            updateScores(rest50Ids,5);
            // updateScores(top20Ids, 15);
            // updateScores(mid30Ids, 10);
            // updateScores(rest50Ids, 5);
        }

        // 5. 构建返回结果：前三名的 questionId -> 票数
        Map<Integer, Integer> finalWorkRankScoreMap = new HashMap<>();
        if (sortedEntries.size() > 0) {
            putEntryToMap(sortedEntries.get(0), finalWorkRankScoreMap,checkDate);
        }
        if (sortedEntries.size() > 1) {
            putEntryToMap(sortedEntries.get(1), finalWorkRankScoreMap,checkDate);
        }
        if (sortedEntries.size() > 2) {
            putEntryToMap(sortedEntries.get(2), finalWorkRankScoreMap,checkDate);
        }

        return finalWorkRankScoreMap;
    }

    @Override
    public List<Map<String, Integer>> getMathMindFirstThree(String school, String lessonName, String subject, String pageName, LocalDate checkDate) {
// 假设已有四个查询结果
        List<PBL_NameScoreDTO> audioOne = pBL_QuestionAnswerMapper.getNameScoreSum(school, lessonName, checkDate, "第一次发现");
        List<PBL_NameScoreDTO> audioTwo = pBL_QuestionAnswerMapper.getNameScoreSum(school, lessonName, checkDate, "第二次发现");
        List<PBL_NameScoreDTO> voteOne  = pBL_QuestionAnswerMapper.getNameScoreSum(school, lessonName, checkDate, "第一次投票");
        List<PBL_NameScoreDTO> voteTwo  = pBL_QuestionAnswerMapper.getNameScoreSum(school, lessonName, checkDate, "第二次投票");

// 合并并汇总分数
        Map<String, Integer> totalScoreMap = new HashMap<>();
        Stream.of(audioOne, audioTwo, voteOne, voteTwo)
                .flatMap(Collection::stream)
                .filter(Objects::nonNull)                      // 防止列表为 null
                .filter(dto -> dto.getUsername() != null)      // 防止用户名为 null
                .forEach(dto -> totalScoreMap.merge(dto.getUsername(), dto.getSumScore(), Integer::sum));

// 取前三名，包装成 List<Map<String, Integer>>
        List<Map<String, Integer>> topThree = totalScoreMap.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue())) // 降序排列
                .limit(3)                                                    // 只取前三
                .map(entry -> {
                    Map<String, Integer> map = new HashMap<>();
                    map.put(entry.getKey(), entry.getValue());
                    return map;
                })
                .collect(Collectors.toList());

// 返回给前端
        return topThree;
    }

    @Override
    public List<Map<String, Integer>> getMathSumFirstThree(String school, String lessonName, String subject, String pageName, LocalDate checkDate) {
        // 获取各环节得分数据
        List<PBL_NameScoreDTO> audioOne = pBL_QuestionAnswerMapper.getNameScoreSum(school, lessonName, checkDate, "第一次发现");
        List<PBL_NameScoreDTO> audioTwo = pBL_QuestionAnswerMapper.getNameScoreSum(school, lessonName, checkDate, "第二次发现");
        List<PBL_NameScoreDTO> voteOne  = pBL_QuestionAnswerMapper.getNameScoreSum(school, lessonName, checkDate, "第一次投票");
        List<PBL_NameScoreDTO> voteTwo  = pBL_QuestionAnswerMapper.getNameScoreSum(school, lessonName, checkDate, "第二次投票");
        List<PBL_NameScoreDTO> finalWork = pBL_QuestionAnswerMapper.getNameScoreSum(school, lessonName, checkDate, "最终作品得分");
        List<PBL_NameScoreDTO> messageSum = chatMessageMapper.getNameScore(checkDate);

        // 合并所有数据源并汇总分数
        Map<String, Integer> totalScoreMap = new HashMap<>();
        Stream.of(audioOne, audioTwo, voteOne, voteTwo, finalWork, messageSum)
                .flatMap(Collection::stream)
                .filter(Objects::nonNull)
                .filter(dto -> dto.getUsername() != null)
                .forEach(dto -> totalScoreMap.merge(dto.getUsername(), dto.getSumScore(), Integer::sum));

        // 取总分前三名
        List<Map<String, Integer>> topThree = totalScoreMap.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(3)
                .map(entry -> {
                    Map<String, Integer> map = new HashMap<>();
                    map.put(entry.getKey(), entry.getValue());
                    return map;
                })
                .collect(Collectors.toList());

        return topThree;
    }




    private void updateScores(List<Integer> top20Ids, int i) {
        for(Integer questionId : top20Ids) {
            //通过questionId拿到对应的voteDTO
            PBL_QuestionItem pblQuestionItem= pBL_QuestionItemMapper.getItemById(questionId);
            if(pblQuestionItem ==null){
                continue;
            }
            //
            PBL_VoteDTO pblVoteDTO = new PBL_VoteDTO();
            pblVoteDTO.setQuestionId(questionId);
            pblVoteDTO.setCheckDate(LocalDate.now());
            pblVoteDTO.setUsername(pblQuestionItem.getUsername());
            pblVoteDTO.setSchool(pblQuestionItem.getSchool());
            pblVoteDTO.setScore(i);
            pblVoteDTO.setCheckDate(LocalDate.now());

            pblVoteDTO.setLessonName(pblQuestionItem.getLessonName());

            pblVoteDTO.setPageName("最终作品得分");
            pBL_QuestionAnswerMapper.deleteByUserNameRank(pblVoteDTO.getUsername(),pblVoteDTO.getSchool(),pblVoteDTO.getLessonName(),pblVoteDTO.getPageName(), LocalDate.now(),i);
            pBL_QuestionAnswerMapper.addItem(pblVoteDTO);
        }
    }


    // 辅助方法：将 Entry 中的 supplementary 转为 questionId 并放入 Map
    private void putEntryToMap(Map.Entry<String, Integer> entry, Map<Integer, Integer> map,LocalDate checkDate) {
        String supplementary = entry.getKey();
        Integer questionId = pBL_QuestionAnswerMapper.getQuestionInfoBySupplementary(supplementary,checkDate);
        if (questionId != null) {
            map.put(questionId, entry.getValue());
        }
    }
}
