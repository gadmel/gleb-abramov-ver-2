import React, {useState, useEffect} from 'react';
import Layout from "../../components/Layout/Layout";
import useAuth from "../../hooks/useAuth";
import LoadingScreen from "../../components/LoadingScreen/LoadingScreen";
import resumeService, {Resume} from "../../services/resumeService";

function ResumePage() {
	const user = useAuth(true)
	const [resume, setResume] = useState<Resume>();

	useEffect(() => {
		resumeService
			.getResume()
			.then((resume: Resume) => setResume(resume))
	}, [user])

	return !resume
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
