import React from 'react'
import {LogoProps} from "./CvPositionLogo";

export type EmploymentPosition = {
	company: string,
	logo: LogoProps,
	jobTitle: string,
	location: string,
	period: string,
	description: string
	skills: string[],
	style: {
		jobTitleClassName: string
	}
}

const Position = (props: EmploymentPosition) => {
	return (
		<div className="cv-position">
			<p className="cv-position--job-title">{props.jobTitle}</p>
			<p className={"cv-position--company " + props.style.jobTitleClassName}>{props.company}</p>
			<p className="cv-position--location">{props.location}</p>
			<p className="cv-position--period">{props.period}</p>
			<p className="cv-position--description">{props.description}</p>
			<div className="skills-list">
				{props.skills?.map((skill: string) => {
					return <div className="skill-tag">{skill}</div>
				})}
			</div>
		</div>
	)
}


export default Position
