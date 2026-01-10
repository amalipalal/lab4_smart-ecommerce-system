package org.example.util.data;

import java.util.List;

public record SqlAndParams(String sql, List<Object> params) {}
