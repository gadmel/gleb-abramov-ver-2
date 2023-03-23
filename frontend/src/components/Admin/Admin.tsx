import React from "react";
import {useNavigate} from 'react-router-dom'
import useAuth from "../../hooks/useAuth";
import Layout from "../Layout/Layout";

function Admin() {
	const user = useAuth()
	const navigate = useNavigate()

	if (user?.role !== "ADMIN") {
		navigate('/')
	}

	return (
		<Layout title="Admin restricted page">
			<section id="restricted">
				<div className="full-screen-unit">Admin</div>
			</section>
		</Layout>
	)
}

export default Admin
