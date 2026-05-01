import { useEffect, useRef, useState, type ChangeEvent } from 'react';
import type { MembersResponse } from '../../types/LoginRequest';

interface GroupSettingsModalProps {
    isOpen: boolean;
    onClose: () => void;
    initialData: {
        gid: number;
        name: string;
        description: string;
        members: Array<{ id: number; name: string; avatar?: string }>;
        role: number;
    };
    onSave: (data: {
        gid: number;
        name: string;
        description: string;
        photo?: File | null;
    }) => void;
    onDelete: (data: {
        gid: number,
        role: number
    }) => void;
    onLeave: (gid: number) => void;
    members: MembersResponse[];
    onAddMemberByUid: (gid: number, uid: number) => Promise<void>;
    onRemoveMember: (gid: number, uid: number) => Promise<void>;
    onGenerateInviteLink: (gid: number) => Promise<string>;
    onSetMemberRole: (gid: number, uid: number, role: number) => Promise<void>;
    onPassOwnership: (gid: number, newOwnerUserId: number) => Promise<void>;
}

export default function GroupSettingsModal({
    isOpen,
    onClose,
    initialData,
    onSave,
    onDelete,
    onLeave,
    members,
    onAddMemberByUid,
    onRemoveMember,
    onGenerateInviteLink,
    onSetMemberRole,
    onPassOwnership
}: GroupSettingsModalProps) {
    const gid = initialData.gid;
    const [name, setName] = useState<string>(initialData.name);
    const [description, setDescription] = useState<string>(initialData.description);
    const [photo, setPhoto] = useState<File | null>(null);
    const [memberUidInput, setMemberUidInput] = useState<string>('');
    const [inviteLink, setInviteLink] = useState<string>('');
    const [isSubmittingAction, setIsSubmittingAction] = useState<boolean>(false);
    const [actionError, setActionError] = useState<string | null>(null);
    const [previewUrl, setPreviewUrl] = useState<string | null>(null);
    const fileInputRef = useRef<HTMLInputElement>(null);
    const role = initialData.role;

    useEffect(() => {
        if (!isOpen) return;

        setName(initialData.name);
        setDescription(initialData.description);
        setPhoto(null);
        setMemberUidInput('');
        setInviteLink('');
        setActionError(null);
        setPreviewUrl(null);
    }, [initialData, isOpen]);

    if (!isOpen) return null;

    const handlePhotoChange = (e: ChangeEvent<HTMLInputElement>) => {
        const file = e.target.files?.[0];
        if (file) {
            setPhoto(file);
            const url = URL.createObjectURL(file);
            setPreviewUrl(url);
        }
    };

    const roleLabel = (roleId: number) => {
        if (roleId === 2) return 'Owner';
        if (roleId === 1) return 'Moderador';
        return 'Miembro';
    };

    const canManageMembers = role >= 1;
    const canManageRoles = role === 2;
    const canDeleteGroup = role === 2;

    const withActionGuard = async (action: () => Promise<void>) => {
        try {
            setActionError(null);
            setIsSubmittingAction(true);
            await action();
        } catch {
            setActionError('No se pudo completar la accion.');
        } finally {
            setIsSubmittingAction(false);
        }
    };

    const handleSubmit = () => {
        onSave({
            gid,
            name,
            description,
            photo
        });
    };

    const triggerFileInput = () => {
        fileInputRef.current?.click();
    };

    const handleAddMemberByUid = async () => {
        const trimmedUid = memberUidInput.trim();
        const parsedUid = Number(trimmedUid);
        if (!trimmedUid || Number.isNaN(parsedUid)) {
            setActionError('Introduce un UID valido.');
            return;
        }
        await withActionGuard(async () => {
            await onAddMemberByUid(gid, parsedUid);
            setMemberUidInput('');
        });
    };

    const handleGenerateInvite = async () => {
        await withActionGuard(async () => {
            const newInviteLink = await onGenerateInviteLink(gid);
            setInviteLink(newInviteLink);
        });
    };

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 font-['Space_Grotesk']">
            <div className="bg-[linear-gradient(160deg,#0a101ff2,#3c1480d9,#142f42e6)] rounded-lg shadow-xl w-full max-w-2xl max-h-[90vh] overflow-hidden flex flex-col">
                <div className="overflow-y-auto grow p-6">
                    <div className="flex justify-between items-center mb-4">
                        <h2 className="text-2xl font-bold text-white">Configuracion del Grupo</h2>
                        <button
                            onClick={onClose}
                            className="text-gray-400 hover:text-white text-2xl"
                        >
                            &times;
                        </button>
                    </div>

                    <div className="space-y-6">
                        {/* 
                        <div className="flex flex-col items-center">
                            <div
                                className="relative cursor-pointer group"
                                onClick={triggerFileInput}
                            >
                                {previewUrl ? (
                                    <img
                                        src={previewUrl}
                                        alt="Preview"
                                        className="w-32 h-32 rounded-full object-cover border-4 border-accent-beige-300"
                                    />
                                ) : (
                                    <div className="w-32 h-32 rounded-full bg-gray-700 flex items-center justify-center border-4 border-dashed border-accent-beige-300">
                                        <span className="text-gray-400 text-sm">Foto</span>
                                    </div>
                                )}
                                <div className="absolute inset-0 bg-black bg-opacity-50 rounded-full flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity">
                                    <span className="text-white text-xs">Cambiar</span>
                                </div>
                            </div>
                            <input
                                type="file"
                                ref={fileInputRef}
                                onChange={handlePhotoChange}
                                accept="image/*"
                                className="hidden"
                            />
                            <p className="mt-2 text-gray-400 text-sm">Haga clic para cambiar la foto</p>
                        </div>
                        */}

                        <div>
                            <label className="block text-gray-300 mb-2">Nombre del grupo</label>
                            <input
                                type="text"
                                value={name}
                                onChange={(e) => setName(e.target.value)}
                                className="w-full bg-gray-800 border border-gray-700 rounded-md px-4 py-2 text-white focus:outline-none focus:ring-2 focus:ring-accent-beige-300"
                            />
                        </div>

                        <div>
                            <label className="block text-gray-300 mb-2">Descripcion</label>
                            <textarea
                                value={description}
                                onChange={(e) => setDescription(e.target.value)}
                                rows={3}
                                className="w-full bg-gray-800 border border-gray-700 rounded-md px-4 py-2 text-white focus:outline-none focus:ring-2 focus:ring-accent-beige-300"
                            />
                        </div>

                        <div>
                            <h3 className="text-lg font-semibold text-white mb-2">Miembros actuales</h3>
                            <div className="space-y-2 max-h-44 overflow-y-auto pr-2">
                                {(Array.isArray(members) ? members : []).filter(Boolean).map((member) => {
                                    const safeUid = Number.isFinite(member.uid) ? member.uid : -1;
                                    const safeName = (member.name ?? '').trim() || `Usuario ${member.uid}`;
                                    const safeRole = Number.isFinite(member.role) ? member.role : 0;

                                    return (
                                    <div key={safeUid} className="flex items-center gap-3 p-2 bg-gray-800 rounded">
                                        <div className="w-8 h-8 rounded-full bg-gray-600 flex items-center justify-center">
                                            <span className="text-xs">{safeName.charAt(0).toUpperCase()}</span>
                                        </div>
                                        <div className="flex grow items-center justify-between gap-3">
                                            <div className="flex flex-col">
                                                <span className="text-white">{safeName}</span>
                                                <span className="text-xs text-gray-400">UID: {safeUid}</span>
                                            </div>
                                            <div className="flex items-center gap-2">
                                                <span className="text-xs rounded bg-black/40 px-2 py-1">{roleLabel(safeRole)}</span>
                                                {canManageRoles && safeRole !== 2 && (
                                                    <select
                                                        value={safeRole === 1 ? 1 : 0}
                                                        onChange={(e) => {
                                                            void withActionGuard(async () => {
                                                                await onSetMemberRole(gid, safeUid, Number(e.target.value));
                                                            });
                                                        }}
                                                        className="bg-gray-700 border border-gray-600 rounded px-2 py-1 text-sm text-white"
                                                    >
                                                        <option value={0}>Miembro</option>
                                                        <option value={1}>Moderador</option>
                                                    </select>
                                                )}
                                                {canManageMembers && (
                                                    <button
                                                        onClick={() => {
                                                            void withActionGuard(async () => {
                                                                await onRemoveMember(gid, safeUid);
                                                            });
                                                        }}
                                                        className="text-red-300 hover:text-red-200 text-sm"
                                                    >
                                                        Eliminar
                                                    </button>
                                                )}
                                                {canManageRoles && safeRole !== 2 && (
                                                    <button
                                                        onClick={() => {
                                                            void withActionGuard(async () => {
                                                                await onPassOwnership(gid, safeUid);
                                                            });
                                                        }}
                                                        className="text-amber-300 hover:text-amber-200 text-sm"
                                                    >
                                                        Ceder ownership
                                                    </button>
                                                )}
                                            </div>
                                        </div>
                                    </div>
                                    );
                                })}
                            </div>
                        </div>

                        <div>
                            <h3 className="text-lg font-semibold text-white mb-2">Invitar o anadir miembros</h3>
                            <div className="flex gap-2">
                                <input
                                    type="text"
                                    value={memberUidInput}
                                    onChange={(e) => setMemberUidInput(e.target.value)}
                                    placeholder="UID de usuario"
                                    className="grow bg-gray-800 border border-gray-700 rounded-md px-4 py-2 text-white focus:outline-none focus:ring-2 focus:ring-accent-beige-300"
                                    onKeyDown={(e) => e.key === 'Enter' && (e.preventDefault(), void handleAddMemberByUid())}
                                />
                                <button
                                    onClick={() => void handleAddMemberByUid()}
                                    disabled={!canManageMembers || isSubmittingAction}
                                    className="bg-accent-beige-300 text-black px-4 py-2 rounded-md font-medium hover:bg-accent-beige-400 transition-colors disabled:opacity-60"
                                >
                                    Anadir
                                </button>
                            </div>

                            <div className="mt-3 flex flex-col gap-2">
                                <div className="flex gap-2">
                                    <button
                                        onClick={() => void handleGenerateInvite()}
                                        disabled={!canManageMembers || isSubmittingAction}
                                        className="bg-accent-beige-300 text-black px-4 py-2 rounded-md font-medium hover:bg-accent-beige-400 transition-colors disabled:opacity-60"
                                    >
                                        Generar enlace invitacion
                                    </button>
                                    {inviteLink && (
                                        <input
                                            type="text"
                                            value={inviteLink}
                                            readOnly
                                            className="grow bg-gray-800 border border-gray-700 rounded-md px-4 py-2 text-white"
                                        />
                                    )}
                                </div>
                            </div>

                            {actionError && <p className="mt-2 text-sm text-red-300">{actionError}</p>}
                        </div>
                    </div>
                </div>

                <div className="border-t border-gray-700 p-4 flex justify-between gap-3">
                    <div className='flex justify-start w-1/3 gap-3'>
                        <button disabled={!canDeleteGroup} onClick={() => onDelete({gid, role})} className="px-6 py-2 border border-gray-600 rounded-md text-white bg-red-500/60 disabled:opacity-50 disabled:cursor-not-allowed font-medium hover:bg-state-error/50 transition-colors">Eliminar grupo </button>
                        <button onClick={() => onLeave(gid)} className="px-6 py-2 border border-gray-600 rounded-md text-white bg-red-500/40  font-medium hover:bg-state-error/50 transition-colors">Abandonar grupo </button>
                    </div>
                    <div className='flex justify-end w-1/2 gap-3'>
                        <button onClick={onClose} className="px-6 py-2 border border-gray-600 rounded-md text-white hover:bg-gray-700 transition-colors" >Cancelar</button>
                        <button onClick={handleSubmit}  className="px-6 py-2 bg-accent-beige-300 text-black rounded-md font-medium hover:bg-accent-beige-400 transition-colors" >Guardar</button>
                    </div>
                </div>
            </div>
        </div>
    );
}
