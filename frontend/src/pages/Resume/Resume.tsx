import React, {useState, useEffect} from 'react';
import {useLocation} from "react-router-dom";
import Layout from "../../components/Layout/Layout";
import useAuth from "../../hooks/useAuth";
import LoadingScreen from "../../components/LoadingScreen/LoadingScreen";
import resumeService, {Resume} from "../../services/resumeService";

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
	}

	function EditResumeLayout(layoutProps: LayoutProps) {
		return (
			<div className="layout-for-edit-mode">
				<div className="overhead left">
					<button onClick={() => layoutProps.setEditMode(!layoutProps.editMode)}>Toggle Edit Mode</button>
				</div>
				{editMode
					? <section id="resume">
						<h1>Resume Page</h1>
						<p>{layoutProps.resume?.id}</p>
						<p>{layoutProps.resume?.name}</p>
						<p>{layoutProps.resume?.invitationSent ? "invitation sent" : "invitation not sent"}</p>
						<p>{layoutProps.resume?.isPublished ? "published" : "not published"}</p>
					</section>
					: <Layout title={"Resume for " + layoutProps.resume?.name + " by Gleb Abramov"}>
						<section id="resume">
							<div className="full-screen-unit">
								<h1>Resume Page</h1>
								<p>{layoutProps.resume?.name}</p>
							</div>
						</section>
					</Layout>}
			</div>
		)
	}


	return isAdminView
		? <EditResumeLayout resume={resume} editMode={editMode} setEditMode={setEditMode}/>
		: !resume
			? <LoadingScreen/>
			: <Layout title={"Resume for " + resume.name + " by Gleb Abramov"}>
				<section id="resume">
					<div className="full-screen-unit">
						<h1>Resume Page</h1>
						<p>{resume.id}</p>
						<p>{resume.name}</p>
					</div>
				</section>
			</Layout>
}

export default ResumePage;


