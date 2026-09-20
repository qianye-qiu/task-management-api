package cn.bugstack.config;

import cn.bugstack.entityEnum.TaskPriority;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import tools.jackson.databind.cfg.CoercionAction;
import tools.jackson.databind.cfg.CoercionInputShape;
import tools.jackson.databind.cfg.EnumFeature;

import java.time.LocalDateTime;

public class RequestJsonConfiguration {

    @Bean
    JsonMapperBuilderCustomizer strictTaskFieldTypes() {
        return builder -> builder
                .enable(EnumFeature.FAIL_ON_NUMBERS_FOR_ENUMS)
                .withCoercionConfig(TaskPriority.class, coercion -> {
                    coercion.setCoercion(CoercionInputShape.EmptyString, CoercionAction.Fail);
                    coercion.setAcceptBlankAsEmpty(true);
                })
                .withCoercionConfig(LocalDateTime.class, coercion -> {
                    coercion.setCoercion(CoercionInputShape.EmptyString, CoercionAction.Fail);
                    coercion.setAcceptBlankAsEmpty(true);
                });
    }
}
