import axios from 'axios';
import { User } from './authenticationService';

export interface Resume {
	id: string;
	name: string;
	userId: string;
	invitationSent: boolean;
	isPublished: boolean;
}

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

	createResume(name: string, userId: string) {
		return axios
			.post('/api/admin/resumes/create/', {name, userId})
			.then((response: { data: Resume }) => {
				return response.data;
			});
	}

}

export default new AdminService();
