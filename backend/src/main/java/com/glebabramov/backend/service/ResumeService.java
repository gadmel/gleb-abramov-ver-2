package com.glebabramov.backend.service;

import com.glebabramov.backend.model.*;
import com.glebabramov.backend.repository.MongoUserRepository;
import com.glebabramov.backend.repository.ResumeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ResumeService {
	private final ResumeRepository repository;
	private final MongoUserRepository userRepository;
	private final IdService idService;
	private final AuthorisationService authorisationService;
	private final VerificationService verificationService;
	private static final String ADMIN_ROLE = "ADMIN";
	private static final String STANDARD_RESUME_ID = "8c687299-9ab7-4f68-8fd9-3de3c521227e";

	@Autowired
	public ResumeService(ResumeRepository repository, MongoUserRepository userRepository, MongoUserDetailsService mongoUserDetailsService, IdService idService) {
		this.repository = repository;
		this.userRepository = userRepository;
		this.idService = idService;
		this.authorisationService = new AuthorisationService(mongoUserDetailsService);
		this.verificationService = new VerificationService(this.userRepository, this.repository);
	}

	public List<Resume> getAllResumes(Principal principal) {
		authorisationService.isAuthorisedByRole(ADMIN_ROLE, "view all resumes", principal);
		return repository.findAll();
	}

	public Resume createResume(ResumeCreateRequest resume, Principal principal) {
		authorisationService.isAuthorisedByRole(ADMIN_ROLE, "create resumes", principal);

		Resume newResume = new Resume(idService.generateId(), resume.name(), resume.addressing(), resume.userIds(), false, false);

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

	public Resume updateResume(ResumeRequest incomingResume, Principal principal) {
		authorisationService.isAuthorisedByRole(ADMIN_ROLE, "update resumes", principal);
		Resume resumeToUpdate = verificationService.resumeDoesExistById(incomingResume.id());

		Set<VerifiedUserResumePair> usersToUnassignResumeFrom = resumeToUpdate.userIds().stream()
				.filter(userId -> !incomingResume.userIds().contains(userId))
				.map(userId -> {
					MongoUser verifiedUserToUnassignResumeFrom = verificationService.userDoesExistById(userId, true);
					Resume previouslyAssignedResume = verificationService.resumeDoesExistById(verifiedUserToUnassignResumeFrom.associatedResume(), true);
					return new VerifiedUserResumePair(verifiedUserToUnassignResumeFrom, previouslyAssignedResume);
				})
				.collect(Collectors.toSet());

		Set<VerifiedUserResumePair> usersToReassignResumeTo = incomingResume.userIds().stream()
				.filter(userId -> !resumeToUpdate.userIds().contains(userId))
				.map(userId -> {
					MongoUser verifiedUserToReassignResumeTo = verificationService.userDoesExistById(userId, true);
					Resume previouslyAssignedResume = verificationService.resumeDoesExistById(verifiedUserToReassignResumeTo.associatedResume(), true);
					return new VerifiedUserResumePair(verifiedUserToReassignResumeTo, previouslyAssignedResume);
				})
				.collect(Collectors.toSet());

		Resume newResume = new Resume(incomingResume.id(), incomingResume.name(), incomingResume.addressing(), incomingResume.userIds(), resumeToUpdate.invitationSent(), resumeToUpdate.isPublished());

		usersToUnassignResumeFrom.forEach(verifiedPair -> {
			repository.save(verifiedPair.resume().unassignFromUser(verifiedPair.user().id()));
			userRepository.save(verifiedPair.user().unassignResume());
			Resume standardResume = verificationService.resumeDoesExistById(STANDARD_RESUME_ID, true);
			repository.save(standardResume.assignToUser(verifiedPair.user().id()));
		});
		usersToReassignResumeTo.forEach(verifiedPair -> {
			Resume previouslyAssignedResumeRefreshed = verificationService.resumeDoesExistById(verifiedPair.resume().id(), true);
			repository.save(previouslyAssignedResumeRefreshed.unassignFromUser(verifiedPair.user().id()));
			userRepository.save(verifiedPair.user().reassignResume(newResume.id()));
		});

		return repository.save(newResume);
	}

	public Resume deleteResume(String id, Principal principal) {
		authorisationService.isAuthorisedByRole(ADMIN_ROLE, "delete resumes", principal);
		Resume resumeToDelete = verificationService.resumeDoesExistById(id);

		resumeToDelete.userIds().stream()
				.map(usersToUnassignResumeFromId -> verificationService.userDoesExistById(usersToUnassignResumeFromId, true))
				.forEach(userToUnassignResumeFrom -> {
					MongoUser updatedUser = userToUnassignResumeFrom.unassignResume();
					userRepository.save(updatedUser);
					Resume resumeToAssignUserTo = verificationService.resumeDoesExistById(updatedUser.associatedResume(), true);
					repository.save(resumeToAssignUserTo.assignToUser(updatedUser.id()));
				});

		repository.deleteById(id);
		return resumeToDelete;
	}

	public Resume getAssociatedResume(Principal principal) {
		return verificationService.whatResumeUserMayAccess(principal);
	}
}
