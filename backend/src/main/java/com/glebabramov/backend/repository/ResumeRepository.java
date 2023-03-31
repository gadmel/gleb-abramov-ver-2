package com.glebabramov.backend.repository;

import com.glebabramov.backend.model.Resume;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResumeRepository extends MongoRepository<Resume, String> {
}
