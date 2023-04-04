package com.glebabramov.backend.service;

import com.glebabramov.backend.model.Resume;
import com.glebabramov.backend.model.ResumeCreateRequest;
import com.glebabramov.backend.model.VerifiedUserResumePair;
import com.glebabramov.backend.repository.MongoUserRepository;
import com.glebabramov.backend.repository.ResumeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ResumeService {
	private final ResumeRepository repository;
	private final MongoUserRepository userRepository;
	private final IdService idService;
	private final AuthorisationService authorisationService;
	private final VerificationService verificationService;
	private static final String ADMIN_ROLE = "ADMIN";

	public List<Resume> getAllResumes(Principal principal) {
		authorisationService.isAuthorisedByRole(ADMIN_ROLE, "view all resumes", principal);
		return repository.findAll()
				.stream()
				.toList();
	}

	public Resume createResume(ResumeCreateRequest resume, Principal principal) {
		authorisationService.isAuthorisedByRole(ADMIN_ROLE, "create resumes", principal);

		Resume newResume = new Resume(idService.generateId(), resume.name(), resume.userIds(), false, false);

		resume.userIds().stream()
				.map(usersToReassignResumeToId -> verificationService.userDoesExistById(usersToReassignResumeToId, true))
				.map(verifiedUserToReassignResumeTo -> {
					Resume previouslyAssignedResume = verificationService.resumeDoesExistById(verifiedUserToReassignResumeTo.associatedResume(), true);
					return new VerifiedUserResumePair(verifiedUserToReassignResumeTo, previouslyAssignedResume);
				})
				.forEach(verifiedPair -> {
							repository.save(verifiedPair.resume().unassignFromUser(verifiedPair.user().id()));
							userRepository.save(verifiedPair.user().reassignResume(newResume.id()));
						}
				);

		return repository.save(newResume);
	}

	public Resume deleteResume(String id, Principal principal) {
		authorisationService.isAuthorisedByRole(ADMIN_ROLE, "delete resumes", principal);
		Resume resumeToDelete = verificationService.resumeDoesExistById(id);

		resumeToDelete.userIds().stream()
				.map(usersToUnassignResumeFromId -> verificationService.userDoesExistById(usersToUnassignResumeFromId, true))
				.forEach(userToUnassignResumeFrom -> userRepository.save(userToUnassignResumeFrom.unassignResume()));

		repository.deleteById(id);
		return resumeToDelete;
	}

}
