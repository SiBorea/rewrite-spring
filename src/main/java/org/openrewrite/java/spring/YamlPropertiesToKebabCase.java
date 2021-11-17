/*
 * Copyright 2021 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openrewrite.java.spring;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.openrewrite.*;
import org.openrewrite.internal.NameCaseConvention;
import org.openrewrite.internal.lang.Nullable;
import org.openrewrite.yaml.YamlIsoVisitor;
import org.openrewrite.yaml.YamlVisitor;
import org.openrewrite.yaml.tree.Yaml;

@Value
@EqualsAndHashCode(callSuper = true)
public class YamlPropertiesToKebabCase extends Recipe {
    @Override
    public String getDisplayName() {
        return "Normalize Spring YAML properties to kebab-case";
    }

    @Override
    public String getDescription() {
        return "Normalize Spring YAML properties to use lowercase and hyphen-separated syntax. " +
                "For example, changing `spring.main.showBanner` to `spring.main.show-banner`. " +
                "With [Spring's relaxed binding](https://docs.spring.io/spring-boot/docs/2.5.6/reference/html/features.html#features.external-config.typesafe-configuration-properties.relaxed-binding), " +
                "`kebab-case` may be used in properties files and still be converted to configuration beans. " +
                "Note, an exception to this is the case of `@Value`, which is match-sensitive. For example, `@Value(\"${anExampleValue}\")` will not match `an-example-value`. " +
                "[The Spring reference documentation recommends using `kebab-case` for properties where possible.](https://docs.spring.io/spring-boot/docs/2.5.6/reference/html/features.html#features.external-config.typesafe-configuration-properties.relaxed-binding)";
    }

    @Option(displayName = "Optional file matcher",
            description = "Matching files will be modified. This is a glob expression.",
            required = false,
            example = "**/application-*.yml")
    @Nullable
    String fileMatcher;

    @Override
    protected TreeVisitor<?, ExecutionContext> getSingleSourceApplicableTest() {
        if (fileMatcher != null) {
            return new HasSourcePath<>(fileMatcher);
        }
        return null;
    }

    @Override
    public YamlVisitor<ExecutionContext> getVisitor() {
        return new YamlIsoVisitor<ExecutionContext>() {
            @Override
            public Yaml.Mapping.Entry visitMappingEntry(Yaml.Mapping.Entry entry, ExecutionContext ctx) {
                Yaml.Mapping.Entry e = super.visitMappingEntry(entry, ctx);
                String key = e.getKey().getValue();
                String asKebabCase = NameCaseConvention.LOWER_HYPHEN.format(key);
                if (!key.equals(asKebabCase)) {
                    return e.withKey(e.getKey().withValue(asKebabCase));
                }
                return e;
            }
        };
    }
}