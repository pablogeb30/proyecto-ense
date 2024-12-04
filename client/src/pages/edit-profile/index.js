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

		if (originalUser.country !== user.country) {
			operations.push({ op: 'replace', path: '/country', value: user.country })
		}

		if (originalUser.picture !== user.picture) {
			operations.push({ op: 'replace', path: '/picture', value: user.picture })
		}

		if (originalUser.name !== user.name) {
			operations.push({ op: 'replace', path: '/name', value: user.name })
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
		<img style = {{ height: '36rem' }} src = { image } alt = { `${ image }` } className = 'absolute top-2 left-0 right-0 w-full object-cover filter blur transform scale-105' />
	</>

}

function Header({ user }) {

	const [updatedName, setUpdatedName] = useState(user?.name || '')

	useEffect(() => {
		setUpdatedName(user.name)
	}, [user])

	const handleNameChange = (e) => {
		setUpdatedName(e.target.value)
		user.name = e.target.value
	}

	return <header className = 'mt-64 relative flex items-end pb-8 mb-8'>
		<img style = {{ aspectRatio: '2/3' }} src = { user.picture } alt = { `${ user.email }` } className = 'w-64 rounded-lg shadow-xl z-20' />
		<hgroup className = 'flex-1'>
			<div className='flex justify-end'>
				<Input
					className = 'text-right text-6xl font-bold p-8'
					type = 'text'
					name = 'name'
					label = 'Nombre'
					labelClassName = 'mb-4'
					variant = 'primary'
					value = { updatedName || '' }
					onChange = { handleNameChange }
				/>
			</div>
			<Tagline user = { user } />
		</hgroup>
	</header>
}

function Tagline({ user }) {

	const [updatedCountry, setUpdatedCountry] = useState(user?.country || '')
	const [updatedPicture, setUpdatedPicture] = useState(user?.picture || '')

	useEffect(() => {
		setUpdatedCountry(user.country)
		setUpdatedPicture(user.picture)
	}, [user])

	const handleCountryChange = (e) => {
		setUpdatedCountry(e.target.value)
		user.country = e.target.value
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
				<Email className = 'w-12 h-12' />
				<p className = 'text-3xl font-semibold pl-4'>
					{ user.email }
				</p>
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
				<Cake className = 'w-12 h-12' />
				<p className = 'text-3xl font-semibold pl-4'>
					{ user.birthday?.day }/{ user.birthday?.month }/{ user.birthday?.year }
				</p>
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