package flyfish.service.impl;

import flyfish.pojo.M_Examination;
import flyfish.pojo.VO.M_QueryClassExaminationVO;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class M_ExaminationServiceImplBoxPlotTest {

    private final M_ExaminationServiceImpl service =
            new M_ExaminationServiceImpl();

    @Test
    void shouldCalculateQuartilesWhiskersAndOutliers() {
        M_QueryClassExaminationVO result =
                new M_QueryClassExaminationVO();

        service.populateBoxPlotStatistics(
                result,
                records("1", "2", "3", "4", "5", "6", "7", "8", "9", "100")
        );

        assertDecimalEquals("3.25", result.getQ1Score());
        assertDecimalEquals("5.5", result.getMedianScore());
        assertDecimalEquals("7.75", result.getQ3Score());
        assertDecimalEquals("1", result.getLowerWhisker());
        assertDecimalEquals("9", result.getUpperWhisker());
        assertEquals(1, result.getOutlierScores().size());
        assertDecimalEquals("100", result.getOutlierScores().get(0));
    }

    @Test
    void shouldUseTheOnlyScoreForEveryBoxPlotValue() {
        M_QueryClassExaminationVO result =
                new M_QueryClassExaminationVO();

        service.populateBoxPlotStatistics(
                result,
                records("80")
        );

        assertDecimalEquals("80", result.getQ1Score());
        assertDecimalEquals("80", result.getMedianScore());
        assertDecimalEquals("80", result.getQ3Score());
        assertDecimalEquals("80", result.getLowerWhisker());
        assertDecimalEquals("80", result.getUpperWhisker());
        assertEquals(Collections.emptyList(), result.getOutlierScores());
    }

    @Test
    void shouldReturnEmptyOutliersWhenThereAreNoScores() {
        M_QueryClassExaminationVO result =
                new M_QueryClassExaminationVO();

        service.populateBoxPlotStatistics(
                result,
                Collections.emptyList()
        );

        assertNull(result.getQ1Score());
        assertNull(result.getMedianScore());
        assertNull(result.getQ3Score());
        assertNull(result.getLowerWhisker());
        assertNull(result.getUpperWhisker());
        assertEquals(Collections.emptyList(), result.getOutlierScores());
    }

    private List<M_Examination> records(String... scores) {
        return Arrays.stream(scores)
                .map(score -> {
                    M_Examination examination = new M_Examination();
                    examination.setScore(new BigDecimal(score));
                    return examination;
                })
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
