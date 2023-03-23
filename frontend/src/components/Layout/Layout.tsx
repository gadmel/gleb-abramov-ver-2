import React from 'react'
import {Link, useLocation} from 'react-router-dom'
import {Helmet} from 'react-helmet'

type Props = {
	title: string
	children: React.ReactNode
}

function Layout(props: Props) {
	const {pathname} = useLocation()
	const isLegalPage = pathname === '/legal'

	return (
		<>
			<Helmet>
				<title>{props.title} - Gleb Abramov</title>
				<meta name="description" content={props.title + " - Gleb Abramov"}/>
				<link rel="canonical" href="https://gleb-abramov.com"/>
				<meta lang="en"/>
				<link rel="apple-touch-icon" href="/apple-touch-icon.png"/>
				<link rel="icon" type="image/png" sizes="32x32" href="/favicon-32x32.png"/>
				<link rel="icon" type="image/png" sizes="16x16" href="/favicon-16x16.png"/>
				<link rel="android-chrome" sizes="192x192" href="/android-chrome-192x192.png"/>
				<link rel="android-chrome" sizes="512x512" href="/android-chrome-512x512.png"/>
				<link rel="manifest" href="/site.webmanifest"/>
			</Helmet>

			<main className="main">
				{props.children}
			</main>

			{!isLegalPage && (
				<footer>
					<p>2023 by Gleb Abramov</p>
					<p><Link to="/legal">Legal Notice</Link> - <Link to="/secured/">Secured</Link></p>
				</footer>
			)}
		</>
	)
}

export default Layout
