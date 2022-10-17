/*
 *  Copyright 2021 The original authors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.kcctl.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.kcctl.service.ConfigInfos.ConfigKeyInfo;

/**
 * This class has identical structure to the {@link ConfigKeyInfo} record, but with
 * getters and setters that allow its content to be mutated after construction.
 * <p>
 * It can be used to manipulate data that was deserialized into a {@link ConfigKeyInfo}
 * instance, without sacrificing the immutability of the {@link ConfigKeyInfo} record class.
 */
public class MutableConfigKeyInfo {

    private String name;
    private String type;
    private boolean required;
    private String defaultValue;
    private String importance;
    private String documentation;
    private String group;
    private int orderInGroup;
    private String width;
    private String displayName;
    private List<String> dependents;

    public MutableConfigKeyInfo(
                                String name,
                                String type,
                                boolean required,
                                String defaultValue,
                                String importance,
                                String documentation,
                                String group,
                                int orderInGroup,
                                String width,
                                String displayName,
                                List<String> dependents) {
        this.name = name;
        this.type = type;
        this.required = required;
        this.defaultValue = defaultValue;
        this.importance = importance;
        this.documentation = documentation;
        this.group = group;
        this.orderInGroup = orderInGroup;
        this.width = width;
        this.displayName = displayName;
        this.dependents = dependents;
    }

    /**
     * Construct a matching {@link MutableConfigKeyInfo} instance from a {@link ConfigKeyInfo} record
     * @param configKeyInfo the {@link ConfigKeyInfo} instance; may not be null
     * @return a matching {@link MutableConfigKeyInfo} for the {@link ConfigKeyInfo} instance;
     * never null
     */
    public static MutableConfigKeyInfo fromRecord(ConfigKeyInfo configKeyInfo) {
        Objects.requireNonNull(configKeyInfo, "configKeyInfo may not be null");
        return new MutableConfigKeyInfo(
                configKeyInfo.name(),
                configKeyInfo.type(),
                configKeyInfo.required(),
                configKeyInfo.defaultValue(),
                configKeyInfo.importance(),
                configKeyInfo.documentation(),
                configKeyInfo.group(),
                configKeyInfo.orderInGroup(),
                configKeyInfo.width(),
                configKeyInfo.displayName(),
                configKeyInfo.dependents() != null ? new ArrayList<>(configKeyInfo.dependents()) : null);
    }

    /**
     * Convert this instance into an immutable {@link ConfigKeyInfo} record.
     * @return a {@link ConfigKeyInfo} whose content matches that of this
     * {@link MutableConfigKeyInfo} instance; never null
     */
    public ConfigKeyInfo toRecord() {
        return new ConfigKeyInfo(
                name,
                type,
                required,
                defaultValue,
                importance,
                documentation,
                group,
                orderInGroup,
                width,
                displayName,
                dependents != null ? new ArrayList<>(dependents) : null);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getImportance() {
        return importance;
    }

    public void setImportance(String importance) {
        this.importance = importance;
    }

    public String getDocumentation() {
        return documentation;
    }

    public void setDocumentation(String documentation) {
        this.documentation = documentation;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public int getOrderInGroup() {
        return orderInGroup;
    }

    public void setOrderInGroup(int orderInGroup) {
        this.orderInGroup = orderInGroup;
    }

    public String getWidth() {
        return width;
    }

    public void setWidth(String width) {
        this.width = width;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public List<String> getDependents() {
        return dependents;
    }

    public void setDependents(List<String> dependents) {
        this.dependents = dependents;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        MutableConfigKeyInfo that = (MutableConfigKeyInfo) o;
        return required == that.required && orderInGroup == that.orderInGroup && Objects.equals(name, that.name) && Objects.equals(type, that.type)
                && Objects.equals(defaultValue, that.defaultValue) && Objects.equals(importance, that.importance) && Objects.equals(documentation, that.documentation)
                && Objects.equals(group, that.group) && Objects.equals(width, that.width) && Objects.equals(displayName, that.displayName)
                && Objects.equals(dependents, that.dependents);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type, required, defaultValue, importance, documentation, group, orderInGroup, width, displayName, dependents);
    }

}
