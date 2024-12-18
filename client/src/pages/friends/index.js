import { Shell, Separator, Button } from '../../components'
import { useUser } from '../../hooks'
import { CakeOutline as Cake, AtSymbolOutline as Email, FlagOutline as Flag } from '@graywolfai/react-heroicons'

import DefaultUserImage from '../profile/images/default_user.png'

export default function Friends() {

	const email = localStorage.getItem('user')
	const { user, updateFriend, deleteFriend } = useUser(email)

	const handleUpdateFriend = (friend, status) => {

		let operations = [{ op: 'replace', path: '/status', value: status }]

		updateFriend(friend, operations)

	}

	const handleDeleteFriend = (friend) => {

		deleteFriend(friend)

	}

	return <>
		<Shell className = 'p-4'>
			<ProfileContent user = { user } updateFriend = { handleUpdateFriend } deleteFriend = { handleDeleteFriend } />
		</Shell>
	</>

}

function ProfileContent({ user, updateFriend, deleteFriend }) {

	return <>

		<div className = 'mx-auto w-full max-w-screen-2xl p-8'>

			<Background image = { user.picture } />

			<Header user = { user } />
			<FriendZone user = { user } updateFriend = { updateFriend } deleteFriend = { deleteFriend } />

		</div>

	</>

}

function Background({ image }) {

	return <>
		<img style = {{ height: '36rem' }} src = { image } alt = { `${ image }` } className = 'absolute top-2 left-0 right-0 w-full object-cover filter blur transform scale-105' />
	</>

}

function Header({ user }) {

	return <header className = 'mt-64 relative flex items-end pb-8 mb-8'>
		<img style = {{ aspectRatio: '2/3', objectFit: 'cover' }} src = { user.picture || DefaultUserImage } alt = { `${ user.email }` } className = 'w-64 rounded-lg shadow-xl z-20' />
		<hgroup className = 'flex-1'>
			<h1 className = {`bg-black bg-opacity-50 backdrop-filter backdrop-blur text-right text-white text-6xl font-bold p-8`}>
				{ user.name }
			</h1>
			<Tagline user = { user } />
		</hgroup>
	</header>
}

function Tagline({ user }) {

	return <>
		<div className = 'flex justify-around'>
			<div className = 'flex items-center align-center'>
				<Email className = 'w-12 h-12' />
				<p className = 'text-3xl font-semibold pl-4'>
					{ user.email }
				</p>
			</div>
			<div className = 'flex items-center align-center'>
				<Flag className = 'w-12 h-12' />
				<p className = 'text-3xl font-semibold pl-4'>
					{ user.country }
				</p>
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

function FriendZone({ user, updateFriend, deleteFriend }) {

	let friends = user?.friends

	let requests = friends?.filter(friend => friend.status === 'PENDING')
	let accepted = friends?.filter(friend => friend.status === 'ACCEPTED')

	return <>
		<h2 className = 'mt-16 font-bold text-2xl'>Solicitudes de amistad</h2>
		<Separator />
		<RequestList requests = { requests || [] } updateFriend = { updateFriend } />
		<h2 className = 'mt-16 font-bold text-2xl'>Amigos</h2>
		<Separator />
		<FriendList friends = { accepted || [] } deleteFriend = { deleteFriend } />
	</>

}

function RequestList({ requests, updateFriend }) {

	if (requests.length === 0) {
		return <>
			<span className = 'block p-8 text-center bg-gray-300 font-bold'>
				No tienes solicitudes de amistad pendientes
			</span>
		</>
	}

	return <>
		<div className = 'w-full flex items-center gap-4'>
			<ul className = 'w-full flex-1 gap-2 relative items-center justify-center'>
				{ requests.map(request =>
					<li key={request.friendEmail} className='bg-white p-4 rounded shadow'>
					<div className='mb-3'>
						<span className='block font-bold text-sm'>Correo:</span>
						<span>{request.friendEmail}</span>
					</div>
					<div className='mb-3'>
						<span className='block font-bold text-sm'>Nombre:</span>
						<span>{request.friendName}</span>
					</div>
					<div className='mb-3'>
						<span className='block font-bold text-sm'>Solicitado el:</span>
						<span>
							{new Date(
							request.requested.year,
							request.requested.month - 1,
							request.requested.day
							).toLocaleDateString()}
						</span>
					</div>
					<div className='flex justify-end'>
						<div className='mr-3'>
							<Button className='mt-2' variant='primary' onClick={() => updateFriend(request.friendEmail, 'ACCEPTED')}>
								Aceptar solicitud
							</Button>
						</div>
						<div>
							<Button className='mt-2' variant='secondary' onClick={() => updateFriend(request.friendEmail, 'DECLINED')}>
								Eliminar solicitud
							</Button>
						</div>
					</div>
				</li>
				)}
			</ul>
		</div>
	</>

}

function FriendList({ friends, deleteFriend }) {

	if (friends.length === 0) {
		return <>
			<span className = 'block p-8 text-center bg-gray-300 font-bold'>
				No tienes amigos aún
			</span>
		</>
	}

	return <>
		<div className = 'w-full flex items-center gap-4'>
			<ul className = 'w-full flex-3 gap-2 relative items-center justify-center'>
				{ friends.map(friend =>
					<li key={friend.friendEmail} className='bg-white p-4 rounded shadow'>
						<div className='mb-3'>
							<span className='block font-bold text-sm'>Correo:</span>
							<span>{friend.friendEmail}</span>
						</div>
						<div className='mb-3'>
							<span className='block font-bold text-sm'>Nombre:</span>
							<span>{friend.friendName}</span>
						</div>
						<div className='mb-3'>
							<span className='block font-bold text-sm'>Aceptado el:</span>
							<span>
								{new Date(
								friend.accepted.year,
								friend.accepted.month - 1,
								friend.accepted.day
								).toLocaleDateString()}
							</span>
						</div>
						<div className='flex justify-end'>
							<Button className='mt-2' variant='primary' onClick={() => deleteFriend(friend.friendEmail)}>
								Eliminar amigo
							</Button>
						</div>
					</li>
				)}
			</ul>
		</div>
	</>

}