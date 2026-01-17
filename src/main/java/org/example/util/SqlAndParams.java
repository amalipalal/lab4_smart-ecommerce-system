package org.example.util;

import java.util.List;

public record SqlAndParams(String sql, List<Object> params) {}
