import axios from 'axios';

export type IncomingUser = {
	id: string;
	username: string;
	role: string;
	associatedResume: string;
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
			.then((response: { data: IncomingUser }) => {
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

	register(username: string, password: string) {
		return axios
			.post('/api/users/register/', {username, password})
			.then((response: { data: IncomingUser }) => {
				return response.data;
			});
	}

	getCurrentUser() {
		return axios
			.get('/api/users/current/')
			.then((response: { data: IncomingUser }) => {
				return response.data;
			});
	}

}

export default new AuthenticationService();
