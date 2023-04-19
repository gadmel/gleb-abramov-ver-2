package com.glebabramov.backend.controller;

import com.glebabramov.backend.model.Resume;
import com.glebabramov.backend.model.ResumeCreateRequest;
import com.glebabramov.backend.model.ResumeRequest;
import com.glebabramov.backend.service.ResumeService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/api")
public class ResumeController {
	private final ResumeService resumeService;

	@GetMapping("/resume/")
	public Resume getResume(Principal principal) {
		return resumeService.getAssociatedResume(principal);
	}

	@GetMapping("/admin/resumes/")
	public List<Resume> getResumes(Principal principal) {
		return resumeService.getAllResumes(principal);
	}

	@PostMapping("/admin/resumes/create/")
	@ResponseStatus(code = HttpStatus.CREATED)
	public Resume createResume(@RequestBody ResumeCreateRequest resume, Principal principal) {
		return resumeService.createResume(resume, principal);
	}

	@PutMapping("/admin/resumes/update/")
	public Resume updateResume(@RequestBody ResumeRequest resume, Principal principal) {
		return resumeService.updateResume(resume, principal);
	}

	@DeleteMapping("/admin/resumes/delete/{id}/")
	public Resume deleteResume(@PathVariable("id") String id, Principal principal) {
		return resumeService.deleteResume(id, principal);
	}

}
