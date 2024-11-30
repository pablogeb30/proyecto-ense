import { ArrowCircleLeftOutline as Back, CakeOutline as Cake, AtSymbolOutline as Email, FlagOutline as Flag, PhotographOutline as Photo, BookmarkAltOutline as Save } from '@graywolfai/react-heroicons'
import { Button, Input, Link, Separator, Shell } from '../../components'
import { CommentList } from '../../components/comment/CommentList'
import { useComments, useUser } from '../../hooks'

import { useEffect, useState } from 'react'

export default function EditProfile() {

	const email = localStorage.getItem('user')
	const { user, status, update } = useUser(email)

	useEffect(() => {
		if (status === 0) {
			alert('El usuario se ha actualizado correctamente')
			window.location.href = '/profile'
		} else if (status === -1) {
			alert('Ha ocurrido un error al actualizar su usuario')
		}
	}, [status]);

	return <Shell className = 'p-4'>

		<ProfileContent user = { user } update = { update } />

	</Shell>

}

function ProfileContent({ user, update }) {

	let originalUser = { ...user }

	const handleSubmit = (e) => {

		e.preventDefault();

		let operations = []

		if (originalUser.email !== user.email) {
			operations.push({ op: 'replace', path: '/email', value: user.email })
		}

		if (originalUser.country !== user.country) {
			operations.push({ op: 'replace', path: '/country', value: user.country })
		}

		if (originalUser.picture !== user.picture) {
			operations.push({ op: 'replace', path: '/picture', value: user.picture })
		}

		if (originalUser.birthday !== user.birthday) {
			operations.push({ op: 'replace', path: '/birthday', value: user.birthday })
		}

		if (operations.length > 0) {
			update(operations)
		}

	};

	return <>

		<div className = 'mx-auto w-full max-w-screen-2xl p-8'>

			<Background image = { user.picture } />

			<Link variant = 'primary' className = 'rounded-full absolute text-white top-4 left-8 flex items-center pl-2 pr-4 py-2 gap-4' to = '/profile'>
				<Back className = 'w-8 h-8'/>
				<span>Volver</span>
			</Link>

			<form onSubmit={ handleSubmit }>

				<div className='absolute top-4 right-8'>
					<Button type = 'submit' variant = 'secondary'>
						<div className='flex flex-row items-center'>
							<Save className = 'w-8 h-8'/>
							Guardar
						</div>
					</Button>
				</div>

				<Header user = { user } />

			</form>

			<Comments user = { user } />

		</div>

	</>

}

function Background({ image }) {

	return <>
		<img style = {{ height: '36rem' }} src = { image } alt = { `${ image } backdrop` } className = 'absolute top-2 left-0 right-0 w-full object-cover filter blur transform scale-105' />
	</>

}

function Header({ user }) {

	return <header className = 'mt-64 relative flex items-end pb-8 mb-8'>
		<img style = {{ aspectRatio: '2/3' }} src = { user.picture } alt = { `${ user.email } profile image` } className = 'w-64 rounded-lg shadow-xl z-20' />
		<hgroup className = 'flex-1'>
			<h1 className = {`bg-black bg-opacity-50 backdrop-filter backdrop-blur text-right text-white text-6xl font-bold p-8`}>
				{ user.name }
			</h1>
			<Tagline user = { user } />
		</hgroup>
	</header>
}

function Tagline({ user }) {

	const [updatedEmail, setUpdatedEmail] = useState(user?.email || '')
	const [updatedCountry, setUpdatedCountry] = useState(user?.country || '')
	const [updatedBirthday, setUpdatedBirthday] = useState(user?.birthday || '')
	const [updatedPicture, setUpdatedPicture] = useState(user?.picture || '')

	useEffect(() => {
		if (user) {
			setUpdatedEmail(user.email)
			setUpdatedCountry(user.country)
			setUpdatedBirthday(user.birthday)
			setUpdatedPicture(user.picture)
		}
	}, [user])

	const handleEmailChange = (e) => {
		setUpdatedEmail(e.target.value)
		user.email = e.target.value
	}

	const handleCountryChange = (e) => {
		setUpdatedCountry(e.target.value)
		user.country = e.target.value
	}

	const handleBirthdayChange = (birthdayObj) => {
		setUpdatedBirthday(birthdayObj)
		user.birthday = birthdayObj
	}

	const handlePictureChange = (e) => {
		setUpdatedPicture(e.target.value)
		user.picture = e.target.value
	}

	return <>
		<div className = 'flex justify-around'>
			<div className = 'flex items-center align-center'>
				<Input
					type = 'url'
					name = 'pictue'
					label = 'Imagen'
					labelClassName = 'mb-4'
					before = { Photo }
					variant = 'primary'
					value = { updatedPicture || '' }
					onChange = { handlePictureChange }
				/>
			</div>
			<div className = 'flex items-center align-center'>
				<Input
					type = 'email'
					name = 'email'
					label = 'Email'
					labelClassName = 'mb-4'
					before = { Email }
					variant = 'primary'
					value = { updatedEmail || '' }
					onChange = { handleEmailChange }
				/>
			</div>
			<div className = 'flex items-center align-center'>
				<Input
					type = 'text'
					name = 'country'
					label = 'País'
					minLength = '2'
					maxLength = '2'
					labelClassName = 'mb-4'
					before = { Flag }
					variant = 'primary'
					value = { updatedCountry || '' }
					onChange = { handleCountryChange }
				/>
			</div>
			<div className = 'flex items-center align-center'>
				<Input
					type = 'date'
					name = 'birthday'
					label = 'Cumpleaños'
					labelClassName = 'mb-4'
					before = { Cake }
					variant = 'primary'
					value =
					{
						updatedBirthday ? new Date(updatedBirthday.year, updatedBirthday.month - 1, updatedBirthday.day, 12, 0, 0).toISOString().split('T')[0] : ''
					}
					onChange = { e =>
						{
							let year, month, day
							[year, month, day] = e.target.value.split('-')
							handleBirthdayChange({ year, month, day })
						}
					}
				/>
			</div>
		</div>
	</>
}

function Comments({ user }) {

	const { comments } = useComments({ filter: { user: user.email } })

	return <>
		<h2 className = 'mt-16 font-bold text-2xl'>Comentarios</h2>
		<Separator />
		<CommentList comments = { comments } variant = 'user' />
	</>

}