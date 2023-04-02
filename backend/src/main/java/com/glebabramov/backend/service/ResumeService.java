package com.glebabramov.backend.service;

import com.glebabramov.backend.model.Resume;
import com.glebabramov.backend.model.ResumeCreateRequest;
import com.glebabramov.backend.repository.ResumeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ResumeService {
	private final ResumeRepository repository;
	private final IdService idService;
	private final AuthorisationService authorisationService;
	private final VerificationService verificationService;
	private static final String ADMIN_ROLE = "ADMIN";

	public List<Resume> getAllResumes(Principal principal) {
		authorisationService.isAuthorisedByRole(ADMIN_ROLE, principal, "view all resumes");
		return repository.findAll()
				.stream()
				.toList();
	}

	public Resume createResume(ResumeCreateRequest resume, Principal principal) {
		authorisationService.isAuthorisedByRole(ADMIN_ROLE, principal, "create resumes");
		Resume newResume = new Resume(idService.generateId(), resume.name(), resume.userId(), false, false);
		return repository.save(newResume);
	}

	public Resume deleteResume(String id, Principal principal) {
		authorisationService.isAuthorisedByRole(ADMIN_ROLE, principal, "delete resumes");
		Resume resumeToDelete = verificationService.resumeDoesExistById(id);

		repository.deleteById(id);
		return resumeToDelete;
	}

}
