import React from 'react'

export type LogoProps = {
	label: string,
	src: string
}

const Logo = (props: LogoProps) => {
	return (
		<img
			src={props.src}
			alt={props.label}
			className="rs-icon"/>
	)
}

export default Logo
