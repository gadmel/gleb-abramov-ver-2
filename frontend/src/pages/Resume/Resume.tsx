import React, {useState, useEffect} from 'react';
import {useLocation} from "react-router-dom";
import Layout from "../../components/Layout/Layout";
import useAuth from "../../hooks/useAuth";
import LoadingScreen from "../../components/LoadingScreen/LoadingScreen";
import resumeService, {Resume} from "../../services/resumeService";
import CollapsibleForm from "../../components/Forms/CollapsibleForm";

function ResumePage() {
	const user = useAuth(true)
	const {state} = useLocation();
	const [editMode, setEditMode] = useState<boolean>(false);
	const [isAdminView, setIsAdminView] = useState<boolean>(false);
	const [resume, setResume] = useState<Resume>();

	useEffect(() => {
		if (state?.resume) {
			setResume(state.resume)
			setIsAdminView(true)
			setEditMode(true)
		} else {
			resumeService
				.getResume()
				.then((resume: Resume) => setResume(resume))
		}
	}, [user])


	type LayoutProps = {
		resume: Resume | undefined
		editMode: boolean
		setEditMode: (editMode: boolean) => void
		children?: React.ReactNode
	}

	function EditResumeLayout(layoutProps: LayoutProps) {
		return (
			<div className="layout-for-edit-mode">
				<div className="overhead left">
					<button onClick={() => layoutProps.setEditMode(!layoutProps.editMode)}>Toggle Edit Mode</button>
				</div>
				{editMode
					? <EditResumeForm resume={layoutProps.resume}/>
					: <DynamicResumeComponent resume={resume}/>}
			</div>
		)
	}

	type EditResumeProps = {
		resume: Resume | undefined
	}

	function EditResumeForm(props: EditResumeProps) {
		const [name, setName] = useState<string>(props.resume?.name || "")

		const handleSubmit = () => {
			console.log("submitting")
		}

		return (
			<Layout title={"Resume for " + props.resume?.name + " by Gleb Abramov"}>
				<section id="resume">
					<div className="full-screen-unit">
						<CollapsibleForm expandedAndFixed formTitle={props.resume?.name || ""} formActionName="Update resume"
											  handleSubmit={handleSubmit}>
							<p>Resume title</p>
							<input type="text" value={name}
									 onChange={(event) => setName(event.target.value)}/>
						</CollapsibleForm>
					</div>
				</section>
			</Layout>
		)

	}


	type DynamicResumeComponentProps = {
		resume: Resume | undefined
	}

	function DynamicResumeComponent(props: DynamicResumeComponentProps) {
		return (
			<Layout title={"Resume for " + props.resume?.name + " by Gleb Abramov"}>
				<section id="resume">
					<div className="full-screen-unit">
						<h1>Resume Page</h1>
						<p>{props.resume?.name}</p>
					</div>
				</section>
			</Layout>
		)
	}


	return isAdminView
		? <EditResumeLayout resume={resume} editMode={editMode} setEditMode={setEditMode}/>
		: !resume
			? <LoadingScreen/>
			: <DynamicResumeComponent resume={resume}/>
}

export default ResumePage;


