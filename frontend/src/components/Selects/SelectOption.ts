import {User} from "../../services/authenticationService";
import {Resume} from "../../services/resumeService";

export type SelectOptionType = {
	value: string
	label: string
}

class SelectOption {
	fromUser(user: User) {
		return {label: user.username, value: user.id}
	}
	fromResume(resume: Resume) {
		return {label: resume.name, value: resume.id}
	}
}

export default new SelectOption
