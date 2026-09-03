package flyfish.service.impl;

import flyfish.mapper.M_ExaminationMapper;
import flyfish.mapper.M_StudentInfoMapper;
import flyfish.pojo.M_Examination;
import flyfish.pojo.VO.M_StudentExamScoreVO;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class M_ExaminationServiceImplStudentScoreTest {

    @Test
    void shouldIncludeCurrentAndPreviousClassStatisticsForEveryStudent() {
        M_ExaminationMapper examinationMapper =
                mock(M_ExaminationMapper.class);
        M_StudentInfoMapper studentInfoMapper =
                mock(M_StudentInfoMapper.class);
        M_ExaminationServiceImpl service =
                new M_ExaminationServiceImpl();
        ReflectionTestUtils.setField(
                service,
                "m_ExaminationMapper",
                examinationMapper
        );
        ReflectionTestUtils.setField(
                service,
                "m_StudentInfoMapper",
                studentInfoMapper
        );

        LocalDateTime currentTime =
                LocalDateTime.of(2026, 8, 15, 9, 0);
        List<M_StudentExamScoreVO> currentScores = Arrays.asList(
                scoreVo("张三", "90", "正常", "已发布"),
                scoreVo("李四", "70", "正常", "已发布"),
                scoreVo("赵六", "70", "正常", "已发布")
        );
        when(examinationMapper.getAllStudentRecord(
                "示例学校", "七年级", 1, "数学", "2026上", "期末考试"
        )).thenReturn(currentScores);
        when(examinationMapper.selectCurrentClassExamRecords(
                "示例学校", "七年级", 1, "数学", "期末考试", "2026上"
        )).thenReturn(Arrays.asList(
                record("张三", "90", "正常", currentTime),
                record("李四", "70", "正常", currentTime),
                record("赵六", "70", "正常", currentTime)
        ));
        when(studentInfoMapper.getStudentNamesBySchoolAndClassName(
                "示例学校", "七年级", 1
        )).thenReturn(Arrays.asList("张三", "李四", "赵六", "王五"));

        M_Examination previousExam = new M_Examination();
        previousExam.setSemester("2026上");
        previousExam.setTestName("期中考试");
        previousExam.setCreateTime(currentTime.minusMonths(2));
        when(examinationMapper.selectPreviousExamInfo(
                "示例学校", "七年级", 1, "数学", currentTime
        )).thenReturn(previousExam);
        when(examinationMapper.selectCurrentClassExamRecords(
                "示例学校", "七年级", 1, "数学", "期中考试", "2026上"
        )).thenReturn(Arrays.asList(
                record("张三", "80", "正常", currentTime.minusMonths(2)),
                record("李四", null, "缺考", currentTime.minusMonths(2)),
                record("赵六", "70", "正常", currentTime.minusMonths(2)),
                record("王五", "70", "正常", currentTime.minusMonths(2))
        ));

        List<M_StudentExamScoreVO> result =
                service.queryAllStudentScore(
                        "示例学校",
                        "七年级",
                        1,
                        "数学",
                        "2026上/期末考试"
                );

        assertEquals(4, result.size());
        assertStudent(result, "张三", "80", "76.67", "73.33", 1, 1);
        assertStudent(result, "李四", null, "76.67", "73.33", 2, null);
        assertStudent(result, "赵六", "70", "76.67", "73.33", 2, 2);
        assertStudent(result, "王五", "70", "76.67", "73.33", null, 2);

        M_StudentExamScoreVO absentStudent = find(result, "王五");
        assertEquals("缺考", absentStudent.getStatus());
        assertEquals("已发布", absentStudent.getHide());

        verify(examinationMapper, never()).insertSingle(
                org.mockito.ArgumentMatchers.any()
        );
    }

    private void assertStudent(
            List<M_StudentExamScoreVO> results,
            String studentName,
            String expectedPreviousScore,
            String expectedAverage,
            String expectedPreviousAverage,
            Integer expectedRank,
            Integer expectedPreviousRank
    ) {
        M_StudentExamScoreVO result = find(results, studentName);
        assertDecimalEquals(expectedAverage, result.getAverageScore());
        assertDecimalEquals(
                expectedPreviousAverage,
                result.getPreviousAverageScore()
        );
        assertEquals(
                "2026上/期末考试",
                result.getCurrentExamName()
        );
        assertEquals("2026上/期中考试", result.getPreviousExamName());
        assertEquals(expectedRank, result.getRankLevel());
        assertEquals(
                expectedPreviousRank,
                result.getPreviousRankLevel()
        );

        if (expectedPreviousScore == null) {
            assertNull(result.getPreviousScore());
        } else {
            assertDecimalEquals(
                    expectedPreviousScore,
                    result.getPreviousScore()
            );
        }
    }

    private M_StudentExamScoreVO find(
            List<M_StudentExamScoreVO> results,
            String studentName
    ) {
        return results.stream()
                .filter(result ->
                        studentName.equals(result.getStudentName())
                )
                .findFirst()
                .orElseThrow();
    }

    private M_StudentExamScoreVO scoreVo(
            String studentName,
            String score,
            String status,
            String hide
    ) {
        M_StudentExamScoreVO result = new M_StudentExamScoreVO();
        result.setStudentName(studentName);
        result.setScore(new BigDecimal(score));
        result.setStatus(status);
        result.setHide(hide);
        return result;
    }

    private M_Examination record(
            String studentName,
            String score,
            String status,
            LocalDateTime createTime
    ) {
        M_Examination result = new M_Examination();
        result.setStudentName(studentName);
        result.setScore(
                score == null ? null : new BigDecimal(score)
        );
        result.setStatus(status);
        result.setCreateTime(createTime);
        return result;
    }

    private void assertDecimalEquals(
            String expected,
            BigDecimal actual
    ) {
        assertEquals(
                0,
                new BigDecimal(expected).compareTo(actual)
        );
    }
}
