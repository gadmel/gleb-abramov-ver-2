import {User} from "../../services/authenticationService";
import {Resume} from "../../services/adminService";

export type SelectOptionType = {
	value: string
	label: string
}

let SelectOptionType = {} as SelectOptionType;

class SelectOption {

	type(): SelectOptionType {
		return SelectOptionType
	}

	fromUser(user: User) {
		return {label: user.username, value: user.id}
	}

	fromResume(resume: Resume) {
		return {label: resume.name, value: resume.id}
	}

}

export default new SelectOption
