package flyfish.service.impl;

import flyfish.mapper.M_DefaultConfigMapper;
import flyfish.mapper.M_ExaminationMapper;
import flyfish.mapper.M_StudentInfoMapper;
import flyfish.pojo.DTO.M_QueryExaminationDTO;
import flyfish.pojo.M_Examination;
import flyfish.pojo.M_StudentInfo;
import flyfish.pojo.VO.M_QueryClassExaminationVO;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class M_ExaminationServiceImplGradeOverviewTest {

    @Test
    void shouldCalculateTheWholeGradeAsOneOverview() {
        M_ExaminationMapper examinationMapper =
                mock(M_ExaminationMapper.class);
        M_StudentInfoMapper studentInfoMapper =
                mock(M_StudentInfoMapper.class);
        M_DefaultConfigMapper defaultConfigMapper =
                mock(M_DefaultConfigMapper.class);

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
        ReflectionTestUtils.setField(
                service,
                "m_DefaultConfigMapper",
                defaultConfigMapper
        );

        LocalDateTime currentTime =
                LocalDateTime.of(2026, 8, 10, 9, 0);
        List<M_Examination> currentRecords = Arrays.asList(
                record("95", "正常", currentTime),
                record("85", "正常", currentTime),
                record("70", "正常", currentTime),
                record("50", "正常", currentTime),
                record("30", "正常", currentTime),
                record(null, "免考", currentTime)
        );

        when(examinationMapper.selectGradeExamRecords(
                "示例学校", "七年级", "数学", "期中考试", "2026上"
        )).thenReturn(currentRecords);
        when(studentInfoMapper.getStudentInfoListBySchoolAndGrade(
                "示例学校", "七年级"
        )).thenReturn(students(7));
        when(defaultConfigMapper.selectRankIntConfigList(
                "示例学校", "张老师", "考试排名等级"
        )).thenReturn(Arrays.asList(5, 25, 50, 75, 100));

        M_Examination previousExam = new M_Examination();
        previousExam.setSemester("2025下");
        previousExam.setTestName("期末考试");
        previousExam.setCreateTime(currentTime.minusMonths(2));
        when(examinationMapper.selectPreviousGradeExamInfo(
                "示例学校",
                "七年级",
                "数学",
                "2026上",
                "期中考试",
                currentTime
        )).thenReturn(previousExam);
        when(examinationMapper.selectGradeExamRecords(
                "示例学校", "七年级", "数学", "期末考试", "2025下"
        )).thenReturn(Arrays.asList(
                record("60", "正常", currentTime.minusMonths(2)),
                record("80", "正常", currentTime.minusMonths(2))
        ));

        M_QueryExaminationDTO request =
                new M_QueryExaminationDTO();
        request.setSchool("示例学校");
        request.setGrade("七年级");
        request.setSubject("数学");
        request.setExamName("2026上/期中考试");
        request.setTeacherName("张老师");

        M_QueryClassExaminationVO result =
                service.queryGradeExamination(request);

        assertEquals("2026上/期中考试", result.getExamtName());
        assertEquals("七年级", result.getGrade());
        assertNull(result.getClassName());
        assertEquals(5, result.getActualTestNum());
        assertEquals(1, result.getAbsentTestNum());
        assertEquals(1, result.getExemptedTestNum());
        assertDecimalEquals("66", result.getAverageScore());
        assertDecimalEquals("66", result.getGrageAverage());
        assertDecimalEquals("95", result.getMaxScore());
        assertDecimalEquals("30", result.getMinScore());
        assertEquals(1, result.getDistinctionNum());
        assertEquals(1, result.getAboveAverageNum());
        assertEquals(1, result.getAverageNum());
        assertEquals(2, result.getBelowAverageNum());
        assertEquals(1, result.getWatchListNum());
        assertEquals(1, result.getRankAPlus());
        assertEquals(1, result.getRankA());
        assertEquals(1, result.getRankB());
        assertEquals(1, result.getRankC());
        assertEquals(1, result.getRankD());
        assertDecimalEquals("50", result.getQ1Score());
        assertDecimalEquals("70", result.getMedianScore());
        assertDecimalEquals("85", result.getQ3Score());
        assertEquals(Collections.emptyList(), result.getOutlierScores());
        assertEquals("2025下/期末考试", result.getPriviousExamName());
        assertDecimalEquals("70", result.getPrivisousAverageScore());
    }

    private M_Examination record(
            String score,
            String status,
            LocalDateTime createTime
    ) {
        M_Examination examination = new M_Examination();
        examination.setScore(
                score == null ? null : new BigDecimal(score)
        );
        examination.setStatus(status);
        examination.setCreateTime(createTime);
        return examination;
    }

    private List<M_StudentInfo> students(int count) {
        return IntStream.range(0, count)
                .mapToObj(index -> new M_StudentInfo())
                .collect(Collectors.toList());
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
