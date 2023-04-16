import axios from 'axios';
import {User} from './authenticationService';
import {Resume} from "./resumeService";

class AdminService {
	getAllUsers() {
		return axios
			.get('/api/admin/users/')
			.then((response: { data: User[] }) => {
				return response.data;
			})
			.catch(error => {
				console.log(error)
				return []
			});
	}

	registerUser(username: string, password: string) {
		return axios
			.post('/api/admin/users/register/', {username, password})
			.then((response: { data: User }) => {
				return response.data;
			});
	}

	updateUser(id: string, username: string, associatedResume: string) {
		return axios
			.put(`/api/admin/users/update/`, {id, username, associatedResume})
			.then((response: { data: User }) => {
				return response.data;
			});
	}

	deleteUser(id: string) {
		return axios
			.delete(`/api/admin/users/delete/${id}/`)
			.then((response: { data: User }) => {
				return response.data;
			});
	}

	getAllResumes() {
		return axios
			.get('/api/admin/resumes/')
			.then((response: { data: Resume[] }) => {
				return response.data;
			});
	}

	createResume(name: string, userIds: string[]) {
		return axios
			.post('/api/admin/resumes/create/', {name, userIds})
			.then((response: { data: Resume }) => {
				return response.data;
			});
	}

	updateResume(id: string, name: string, userIds: string[]) {
		return axios
			.put(`/api/admin/resumes/update/`, {id, name, userIds})
			.then((response: { data: Resume }) => {
				return response.data;
			});
	}

	deleteResume(id: string) {
		return axios
			.delete(`/api/admin/resumes/delete/${id}/`)
			.then((response: { data: Resume }) => {
				return response.data;
			});
	}

}

export default new AdminService();
