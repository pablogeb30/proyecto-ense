import { Shell, Separator, Button } from '../../components'
import { CommentList } from '../../components/comment/CommentList'
import { useUser, useComments } from '../../hooks'
import { CakeOutline as Cake, AtSymbolOutline as Email, FlagOutline as Flag } from '@graywolfai/react-heroicons'

export default function Profile() {

	const email = localStorage.getItem('user')
	const { user } = useUser(email)

	return <Shell className = 'p-4'>
		<ProfileContent user = { user } />
	</Shell>

}

function ProfileContent({ user }) {

	return <>
		<div className = 'mx-auto w-full max-w-screen-2xl p-8'>
			<Background image = { user.picture } />
			<Header user = { user } />
			<Comments user = { user } />
		</div>
	</>

}

function Background({ image }) {

	return <>
		<img style = {{ height: '36rem' }} src = { image } alt = { `${ image } backdrop` } className = 'absolute top-2 left-0 right-0 w-full object-cover filter blur transform scale-105'
		/>
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

function Comments({ user }) {

	const { comments } = useComments({ filter: { user: user.email } })

	return <>
		<h2 className = 'mt-16 font-bold text-2xl'>Comentarios</h2>
		<Separator />
		<CommentList comments = { comments } variant = 'user' />
	</>

}