package com.glebabramov.backend.controller;

import com.glebabramov.backend.model.Resume;
import com.glebabramov.backend.model.ResumeCreateRequest;
import com.glebabramov.backend.service.ResumeService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/api/admin/resumes")
public class ResumeController {
	private final ResumeService resumeService;

	@GetMapping("/")
	public List<Resume> getResumes(Principal principal) {
		return resumeService.getAllResumes(principal);
	}

	@PostMapping("/create/")
	@ResponseStatus(code = HttpStatus.CREATED)
	public Resume createResume(@RequestBody ResumeCreateRequest resume, Principal principal) {
		return resumeService.createResume(resume, principal);
	}
}
