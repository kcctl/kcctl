package org.kcctl.service;

public record Offset(int partition, long offset, String key) {}

