import React from "react";
import {Resume} from "../../services/resumeService";
import {Timeline} from 'rsuite';
import Layout from "../Layout/Layout";
import OverheadButtonHome from "../Navigation/OverheadButtonHome";
import OverheadButtonBack from "../Navigation/OverheadButtonBack";
import Position, {EmploymentPosition} from "./CvPosition";
import Logo from "./CvPositionLogo";
import staticDataExperience from "../../data/staticDataCvPositions.json";
import staticDataKnowHowGeneral from "../../data/staticDataTechSkillsGeneral.json";
import staticDataKnowHowFrontend from "../../data/staticDataTechSkillsFrontend.json";
import staticDataKnowHowBackend from "../../data/staticDataTechSkillsBackend.json";
import staticDataKnowHowCommonTools from "../../data/staticDataTechSkillsCommonTools.json";
import staticDataKnowHowShallowKnowledge from "../../data/staticDataTechSkillsInterests.json";
import glebAbramov from "../../assets/gleb_photo_squared.jpg";
import linkedIn from "../../assets/linkedin-logo-png-transparent.png";
import github from "../../assets/github-logo.png";
import envelope from "../../assets/envelope.png";

type Props = {
	resume: Resume
}

function ViewResumePage(props: Props) {

	return (
		<Layout title={"Resume for " + props.resume?.name + " by Gleb Abramov"}>
			<OverheadButtonHome/>
			<OverheadButtonBack/>
			<section id="resume">
				<div className="full-screen-unit">

					<h1>Resume</h1>
					<div className="summary">
						<div className="summary__avatar--wrapper">
							<img src={glebAbramov} alt="Gleb Abramov" className="summary__avatar"/>
						</div>
						<div className="summary__stats">
							<div className="summary__name">Gleb Abramov</div>
							<div className="summary__job-title">Fullstack Software Developer</div>
							<div className="summary__links">
								<a href="https://github.com/gadmel" target="_blank" rel="noreferrer">
									<div className="summary__links--button github">
										<img src={github} alt="github"/>
										<span>GitHub</span>
									</div>
								</a>
								<a href="https://www.linkedin.com/in/g-abramov/" target="_blank" rel="noreferrer">
									<div className="summary__links--button linkedin">
										<img src={linkedIn} alt="LinkedIn"/>
									</div>
								</a>
								<a href="mailto:abramov.gleb@gmail.com" target="_blank" rel="noreferrer">
									<div className="summary__links--button email">
										<img src={envelope} alt="E-mail"/>
										<span>E-mail</span>
									</div>
								</a>
							</div>
						</div>
					</div>

					<h2>Profile</h2>
					<div className="dynamic">
						<p>{props.resume?.name}</p>
					</div>

					<div className="qualification">

						<div className="soft-skills">
							<h3>Soft Skills</h3>
							<div>Analytical thinking</div>
							<div>Resolving issues</div>
							<div>Result oriented</div>
							<div>Adaptability</div>
							<div>Willingness to learn</div>

							<h3>Communication</h3>
							<div>English: fluent</div>
							<div>German: fluent</div>
							<div>Russian: native</div>
							<div>French: basic</div>

							<h3>Academic background</h3>
							<div>2013-2015, Logistics and Mobility, TUHH</div>
							<div>2010-2012, Media Technics, HS Emden-Leer</div>
						</div>

						<div className="tech-know-how">
							<h3>Know-how</h3>
							General concepts:
							<div className="skills-list">
								{staticDataKnowHowGeneral.map((skill: string) => {
									return <div className="skill-tag" key={skill}>{skill}</div>
								})}
							</div>
							Backend:
							<div className="skills-list">
								{staticDataKnowHowBackend.map((skill: string) => {
									return <div className="skill-tag" key={skill}>{skill}</div>
								})}
							</div>
							Frontend:
							<div className="skills-list">
								{staticDataKnowHowFrontend.map((skill: string) => {
									return <div className="skill-tag" key={skill}>{skill}</div>
								})}
							</div>
							Common tools:
							<div className="skills-list">
								{staticDataKnowHowCommonTools.map((skill: string) => {
									return <div className="skill-tag" key={skill}>{skill}</div>
								})}
							</div>
							Shallow knowledge and field of interest:
							<div className="skills-list">
								{staticDataKnowHowShallowKnowledge.map((skill: string) => {
									return <div className="skill-tag" key={skill}>{skill}</div>
								})}
							</div>
						</div>

					</div>

					<div className="cv-container">
						<h1>Experience</h1>
						<Timeline className="cv">
							{staticDataExperience?.map((employment: EmploymentPosition) => {
								return (
									<Timeline.Item dot={<Logo {...employment.logo}/>} key={employment.company}>
										<Position {...employment}/>
									</Timeline.Item>
								)
							})}
						</Timeline>
					</div>

					<h1>Contact</h1>
					<div className="contact">
						<p>Gleb Abramov
							<br/>Försterweg 167
							<br/>22525 Hamburg
							<br/>Germany
						</p>
						<p>Tel: +49 173 247 0865</p>
					</div>

				</div>

			</section>
		</Layout>
	)
}

export default ViewResumePage
