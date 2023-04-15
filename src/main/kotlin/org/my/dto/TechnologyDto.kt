package org.my.dto

class TechnologyDto(
    val dependencies: List<String>,
    val configurations: List<String>,
    val annotations: Map<String, String>,   // annotation name and where to use
    val beans: List<String>,
)