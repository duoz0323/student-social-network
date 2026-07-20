package com.stu.edu.vn.backend.auth.generator;

/** Sinh registration flow token opaque để chỉ trả raw token cho Client một lần. */
public interface FlowTokenGenerator {

    String generate();
}
