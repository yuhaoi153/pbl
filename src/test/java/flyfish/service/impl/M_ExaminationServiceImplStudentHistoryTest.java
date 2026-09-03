package flyfish.service.impl;

import flyfish.mapper.M_ExaminationMapper;
import flyfish.pojo.DTO.M_QueryStudentAllExamDTO;
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
import static org.mockito.Mockito.when;

class M_ExaminationServiceImplStudentHistoryTest {

    @Test
    void shouldReturnStudentExamHistoryFromOldestToNewest() {
        M_ExaminationMapper examinationMapper =
                mock(M_ExaminationMapper.class);
        M_ExaminationServiceImpl service =
                new M_ExaminationServiceImpl();
        ReflectionTestUtils.setField(
                service,
                "m_ExaminationMapper",
                examinationMapper
        );

        LocalDateTime firstExamTime =
                LocalDateTime.of(2025, 12, 20, 9, 0);
        LocalDateTime secondExamTime =
                LocalDateTime.of(2026, 3, 20, 9, 0);
        LocalDateTime thirdExamTime =
                LocalDateTime.of(2026, 6, 20, 9, 0);

        // 故意按倒序返回，验证service仍会按时间升序整理。
        when(examinationMapper.selectAllClassExamHistoryRecords(
                "示例学校", "七年级", 1, "数学"
        )).thenReturn(Arrays.asList(
                record(5L, "张三", "90", "正常", "2026上", "期末考试", thirdExamTime),
                record(6L, "李四", "90", "正常", "2026上", "期末考试", thirdExamTime),
                record(3L, "张三", null, "缺考", "2026上", "期中考试", secondExamTime),
                record(4L, "李四", "70", "正常", "2026上", "期中考试", secondExamTime),
                record(1L, "张三", "80", "正常", "2025下", "期末考试", firstExamTime),
                record(2L, "李四", "90", "正常", "2025下", "期末考试", firstExamTime)
        ));

        M_QueryStudentAllExamDTO request =
                new M_QueryStudentAllExamDTO();
        request.setSchool("示例学校");
        request.setGrade("七年级");
        request.setClassName(1);
        request.setSubject("数学");
        request.setStudentName("张三");

        List<M_StudentExamScoreVO> result =
                service.queryStudentAllExam(request);

        assertEquals(3, result.size());

        M_StudentExamScoreVO first = result.get(0);
        assertEquals("2025下/期末考试", first.getCurrentExamName());
        assertDecimalEquals("80", first.getScore());
        assertDecimalEquals("85", first.getAverageScore());
        assertEquals(2, first.getRankLevel());
        assertNull(first.getPreviousExamName());
        assertNull(first.getPreviousScore());
        assertNull(first.getPreviousAverageScore());
        assertNull(first.getPreviousRankLevel());

        M_StudentExamScoreVO second = result.get(1);
        assertEquals("2026上/期中考试", second.getCurrentExamName());
        assertEquals("缺考", second.getStatus());
        assertNull(second.getScore());
        assertNull(second.getRankLevel());
        assertEquals("2025下/期末考试", second.getPreviousExamName());
        assertDecimalEquals("80", second.getPreviousScore());
        assertDecimalEquals("85", second.getPreviousAverageScore());
        assertEquals(2, second.getPreviousRankLevel());

        M_StudentExamScoreVO third = result.get(2);
        assertEquals("2026上/期末考试", third.getCurrentExamName());
        assertDecimalEquals("90", third.getScore());
        assertDecimalEquals("90", third.getAverageScore());
        assertEquals(1, third.getRankLevel());
        assertEquals("2026上/期中考试", third.getPreviousExamName());
        assertNull(third.getPreviousScore());
        assertDecimalEquals("70", third.getPreviousAverageScore());
        assertNull(third.getPreviousRankLevel());
    }

    private M_Examination record(
            Long id,
            String studentName,
            String score,
            String status,
            String semester,
            String testName,
            LocalDateTime createTime
    ) {
        M_Examination result = new M_Examination();
        result.setId(id);
        result.setStudentName(studentName);
        result.setScore(
                score == null ? null : new BigDecimal(score)
        );
        result.setStatus(status);
        result.setSemester(semester);
        result.setTestName(testName);
        result.setCreateTime(createTime);
        result.setHide("已发布");
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
