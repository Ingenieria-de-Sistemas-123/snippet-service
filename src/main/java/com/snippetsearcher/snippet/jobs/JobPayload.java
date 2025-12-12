package com.snippetsearcher.snippet.jobs;

import java.util.UUID;

public record JobPayload(JobType type, UUID adminId, UUID snippetId) {}
