import axios from 'axios';

export interface User {
	readonly id: string;
	readonly username: string;
	readonly role: string;
	readonly associatedResume: string;
}


class AuthenticationService {
	login(username: string, password: string) {
		return axios
			.post(
				'/api/users/login/',
				{},
				{
					headers: {
						Authorization: `Basic ${window.btoa(`${username}:${password}`)}`,
					},
				})
			.then((response: { data: User }) => {
				return response.data;
			});
	}

	logout() {
		return axios
			.post('/api/users/logout/')
			.catch(error => {
				console.log(error)
			})
	}

	getCurrentUser() {
		return axios
			.get('/api/users/current/')
			.then((response: { data: User }) => {
				return response.data;
			});
	}

}

export default new AuthenticationService();
