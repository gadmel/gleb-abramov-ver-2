import React from 'react'
import {Link, useLocation} from 'react-router-dom'

type Props = {
	children: React.ReactNode
}

function Layout(props: Props) {
	const {pathname} = useLocation()
	const isLegalPage = pathname === '/legal'

	return (
		<>
			<main className="main">
				{props.children}
			</main>

			{!isLegalPage && (
				<footer>
					<p>2023 by Gleb Abramov - <Link to="/legal">Legal Notice</Link></p>
				</footer>
			)}
		</>
	)
}

export default Layout
