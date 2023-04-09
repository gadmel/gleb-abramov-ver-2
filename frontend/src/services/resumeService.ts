import axios from 'axios';

export interface Resume {
	id: string;
	name: string;
	userIds: string[];
	invitationSent: boolean;
	isPublished: boolean;
}

class resumeService {

	getResume() {
		return axios
			.get('/api/resume/')
			.then((response: { data: Resume }) => {
				return response.data;
			});
	}

}

export default new resumeService();
