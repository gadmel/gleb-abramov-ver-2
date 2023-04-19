import React from "react";
import {useNavigate} from "react-router-dom";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faEdit, faEye} from "@fortawesome/free-solid-svg-icons";
import {Resume} from "../../services/resumeService";
import DeleteButtonResume from "../Buttons/InlineButtonDeleteResume";
import {User} from "../../services/authenticationService";
import CollapsibleFormCreateResume from "../Forms/CollapsibleFormCreateResume";
import {SelectOptionType} from "../Selects/SelectOption";

type Props = {
	resumes: Resume[]
	users: User[]
	refreshData: () => void
	usersSelectOptions: SelectOptionType[]
}

function ListResumes(props: Props) {
	const standardResumeId = "8c687299-9ab7-4f68-8fd9-3de3c521227e"
	const navigate = useNavigate()

	const handleNavigateToEditPage = (resume: Resume) => {
		navigate("/secured/cv/", {state: {resume, editMode: true}})
	}

	const handleNavigateToViewPage = (resume: Resume) => {
		navigate("/secured/cv/", {state: {resume, editMode: false}})
	}

	const listAssignedUsersNames = (resume: Resume) => {
		return props.users.filter((user: User) => resume.userIds?.includes(user.id))
			.map((user: User) => user.username)
			.join(", ")
	}

	return (
		<>
			<h3>Resumes</h3>
			{props.resumes.map((resume: Resume) => (
					<div className="resume" key={resume.id}>
						<p>{resume.name}</p>
						<p>{listAssignedUsersNames(resume)}</p>
						<div className="action-controls">
							<button className="action-button action-button--standard"
									  onClick={() => handleNavigateToViewPage(resume)}>
								<FontAwesomeIcon icon={faEye}/>
							</button>
							<button className="action-button action-button--standard"
									  onClick={() => handleNavigateToEditPage(resume)}>
								<FontAwesomeIcon icon={faEdit}/>
							</button>
							{resume.id !== standardResumeId &&
                         <DeleteButtonResume id={resume.id} refreshData={props.refreshData}/>}
						</div>
					</div>
				)
			)}

			<CollapsibleFormCreateResume usersSelectOptions={props.usersSelectOptions} refreshData={props.refreshData}/>
		</>
	)
}

export default ListResumes
