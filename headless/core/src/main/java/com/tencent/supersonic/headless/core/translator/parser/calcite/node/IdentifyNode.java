package com.tencent.supersonic.headless.core.translator.parser.calcite.node;

import com.tencent.supersonic.common.pojo.enums.EngineType;
import com.tencent.supersonic.headless.core.translator.parser.s2sql.Identify;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.validate.SqlValidatorScope;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class IdentifyNode extends SemanticNode {

    public static SqlNode build(Identify identify, SqlValidatorScope scope, EngineType engineType)
            throws Exception {
        return parse(identify.getName(), scope, engineType);
    }

    public static Set<String> getIdentifyNames(List<Identify> identifies, Identify.Type type) {
        return identifies.stream().filter(i -> type.name().equalsIgnoreCase(i.getType()))
                .map(i -> i.getName()).collect(Collectors.toSet());
    }

    public static boolean isForeign(String name, List<Identify> identifies) {
        Optional<Identify> identify =
                identifies.stream().filter(i -> i.getName().equalsIgnoreCase(name)).findFirst();
        if (identify.isPresent()) {
            return Identify.Type.FOREIGN.name().equalsIgnoreCase(identify.get().getType());
        }
        return false;
    }

    public static boolean isPrimary(String name, List<Identify> identifies) {
        Optional<Identify> identify =
                identifies.stream().filter(i -> i.getName().equalsIgnoreCase(name)).findFirst();
        if (identify.isPresent()) {
            return Identify.Type.PRIMARY.name().equalsIgnoreCase(identify.get().getType());
        }
        return false;
    }
}
