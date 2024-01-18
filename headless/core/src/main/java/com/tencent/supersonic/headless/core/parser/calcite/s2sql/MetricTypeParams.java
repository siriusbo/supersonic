package com.tencent.supersonic.headless.core.parser.calcite.s2sql;

import java.util.List;
import lombok.Data;

@Data
public class MetricTypeParams {

    private List<Measure> measures;
    private List<Measure> metrics;
    private List<Measure> fields;

    private String expr;

}