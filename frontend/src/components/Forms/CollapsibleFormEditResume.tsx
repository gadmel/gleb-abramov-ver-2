import React from "react";
import {useMediaQuery} from 'react-responsive'
import {useNavigate} from "react-router-dom";
import Select from "react-select";
import Layout from "../Layout/Layout";
import CollapsibleForm from "./CollapsibleForm";
import {Resume} from "../../services/resumeService";
import adminService from "../../services/adminService";
import {SelectOptionType} from "../Selects/SelectOption";
import {selectStyles, selectTheme} from "../Selects/SelectStyles";

type Props = {
	resume: Resume
	name: string
	setName: (name: string) => void
	userIds: string[]
	setUserIds: (userIds: string[]) => void
	usersSelectOptions: SelectOptionType[]
	isStandardResume: boolean
}

function CollapsibleFormEditResume(props: Props) {
	const navigate = useNavigate()
	const systemPrefersLight = useMediaQuery({query: '(prefers-color-scheme: light)'})

	const handleUpdateResume = (event: React.FormEvent<HTMLFormElement>) => {
		event.preventDefault()
		adminService
			.updateResume(props.resume.id, props.name, props.userIds)
			.then((resume: Resume) => {
				navigate("/secured/cv/", {state: {resume}})
			})
	}
	const handleNavigateToAdminPanel = () => {
		navigate("/secured/")
	}

	return (
		<Layout title={"Resume for " + props.resume.name + " by Gleb Abramov"}>
			<section id="resume">
				<div className="full-screen-unit">
					<CollapsibleForm expandedAndFixed
										  formTitle={props.resume.name || ""}
										  formActionName="Update resume"
										  handleSubmit={handleUpdateResume}
										  handleCancel={handleNavigateToAdminPanel}>
						<p>Resume title</p>
						<input type="text"
								 className="form__input"
								 value={props.name}
								 onChange={(event) => props.setName(event.target.value)}/>

						{!props.isStandardResume && <>
                      <p>Assigned users</p>
                      <Select isMulti
                              options={props.usersSelectOptions}
                              value={props.usersSelectOptions.filter((option: SelectOptionType) => props.userIds.includes(option.value)) || null}
                              onChange={(options) => props.setUserIds(options.map(option => option.value))}
                              className="user-select"
                              classNamePrefix="user-select"
                              styles={selectStyles}
                              theme={selectTheme(systemPrefersLight ? 'light' : 'dark')}/>
                  </>}
					</CollapsibleForm>
				</div>
			</section>
		</Layout>
	)

}

export default CollapsibleFormEditResume
