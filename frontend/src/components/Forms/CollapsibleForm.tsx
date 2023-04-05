import React, {useState} from 'react'

type Props = {
	formTitle: string
	formActionName: string
	handleSubmit: (event: React.FormEvent<HTMLFormElement>) => void
	handleCancel?: () => void
	expandedAndFixed?: boolean
	children: React.ReactNode
}

function CollapsibleForm(props: Props) {
	const [open, setOpen] = useState<boolean>(!!props.expandedAndFixed)

	const toggleForm = () => {
		setOpen(!open)
	}

	const handleClickTheFormTitle = () => {
		!props.expandedAndFixed && toggleForm()
	}

	return (
		<form className="form" onSubmit={props.handleSubmit}>

			<div className="form__title" onClick={handleClickTheFormTitle}>
				{props.formTitle}
			</div>

			{open && (
				<>
					<div className="form__block">
						{props.children}
					</div>
					<button type="submit" className="form__button">
						{props.formActionName}
					</button>
					{props.handleCancel && (
						<button type="button" className="form__button" onClick={props.handleCancel}>Cancel</button>)}
				</>
			)}

		</form>
	)
}

export default CollapsibleForm
