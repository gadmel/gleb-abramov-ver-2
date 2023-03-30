import axios from 'axios';
import { User } from './authenticationService';

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

	register(username: string, password: string) {
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

}

export default new AdminService();
