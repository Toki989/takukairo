import React from 'react';
import {createRoot} from 'react-dom/client';
import {createBrowserRouter,RouterProvider,Outlet} from 'react-router-dom';
import {useResource,Root,Shell,Protected,Login,Setup,Legal,NotFound,useSession} from './app';
import {ConfirmProvider} from './components/common';
import type {Person} from './api/types';
import {Home,ScenarioView,PcCollection,PersonDialog} from './features/Library';
import {ScenarioForm,PcForm} from './features/Forms';
import {TableForm} from './features/TableForm';
import {TableDetail} from './features/TableDetail';
import {ImportPage} from './features/Import';
import './styles/globals.css';
function Account(){const {session,refresh}=useSession();const people=useResource<Person[]>('/persons');const self=people.data?.find(p=>p.id===session?.user?.selfPerson?.id);const [editing,setEditing]=React.useState(false);return <main className="page form-page"><p className="kicker">ACCOUNT</p><h1>アカウント設定</h1><p>表示名: {session?.user?.selfPerson?.displayName}</p><button disabled={!self} onClick={()=>setEditing(true)}>TRPGで使う名前を編集</button><p>外部画像の利用同意: {session?.user?.externalImageConsentGiven?'確認済み':'未確認'}</p><p className="muted">ローカル固定テストユーザーのアカウントです。</p>{editing&&self&&<PersonDialog person={self} onClose={()=>setEditing(false)} onSaved={()=>{setEditing(false);people.reload();void refresh()}}/>}</main>}
const router=createBrowserRouter([{element:<ConfirmProvider><Root/></ConfirmProvider>,children:[{path:'/dev-login',element:<Login dev/>},{path:'/login',element:<Login/>},{path:'/setup/self-person',element:<Setup/>},{path:'/terms',element:<Legal/>},{path:'/privacy',element:<Legal privacy/>},{element:<Protected><Outlet/></Protected>,children:[{path:'/tables/:id',element:<TableDetail/>},{element:<Shell/>,children:[{index:true,element:<Home/>},{path:'/scenarios/new',element:<ScenarioForm/>},{path:'/scenarios/:id',element:<ScenarioView/>},{path:'/scenarios/:id/edit',element:<ScenarioForm/>},{path:'/scenarios/:scenarioId/tables/new',element:<TableForm/>},{path:'/tables/:id/edit',element:<TableForm/>},{path:'/pcs',element:<PcCollection/>},{path:'/pcs/new',element:<PcForm/>},{path:'/pcs/:id/edit',element:<PcForm/>},{path:'/import',element:<ImportPage/>},{path:'/account',element:<Account/>}]}]},{path:'*',element:<NotFound/>}]}]);
createRoot(document.getElementById('root')!).render(<RouterProvider router={router}/>);
