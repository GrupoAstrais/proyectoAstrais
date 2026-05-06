import { useCallback, useEffect, useState } from 'react';
import type { GroupInvitacionRespuesta, MembersResponse } from '../../types/LoginRequest';
import { API_BASE_URL } from '../../data/Api';

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
    onGenerateInviteLink: (gid: number) => Promise<GroupInvitacionRespuesta>;
    onLoadInvites: (gid: number) => Promise<GroupInvitacionRespuesta[]>;
    onRevokeInvite: (gid: number, code: string) => Promise<void>;
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
    onLoadInvites,
    onRevokeInvite,
    onSetMemberRole,
    onPassOwnership
}: GroupSettingsModalProps) {
    const gid = initialData.gid;
    const [name, setName] = useState<string>(initialData.name);
    const [description, setDescription] = useState<string>(initialData.description);
    const [photo, setPhoto] = useState<File | null>(null);
    const [memberUidInput, setMemberUidInput] = useState<string>('');
    const [latestInviteCode, setLatestInviteCode] = useState<string>('');
    const [latestInviteLink, setLatestInviteLink] = useState<string>('');
    const [latestLegacyRedirectLink, setLatestLegacyRedirectLink] = useState<string>('');
    const [invites, setInvites] = useState<GroupInvitacionRespuesta[]>([]);
    const [isSubmittingAction, setIsSubmittingAction] = useState<boolean>(false);
    const [actionError, setActionError] = useState<string | null>(null);
    const role = initialData.role;

    useEffect(() => {
        if (!isOpen) return;

        setName(initialData.name);
        setDescription(initialData.description);
        setPhoto(null);
        setMemberUidInput('');
        setLatestInviteCode('');
        setLatestInviteLink('');
        setLatestLegacyRedirectLink('');
        setInvites([]);
        setActionError(null);
    }, [initialData, isOpen]);

    const roleLabel = (roleId: number) => {
        if (roleId === 2) return 'Owner';
        if (roleId === 1) return 'Moderador';
        return 'Miembro';
    };

    const canManageMembers = role >= 1;
    const canManageRoles = role === 2;
    const canDeleteGroup = role === 2;

    const withActionGuard = useCallback(async (action: () => Promise<void>) => {
        try {
            setActionError(null);
            setIsSubmittingAction(true);
            await action();
        } catch {
            setActionError('No se pudo completar la accion.');
        } finally {
            setIsSubmittingAction(false);
        }
    }, []);

    const handleSubmit = () => {
        onSave({
            gid,
            name,
            description,
            photo
        });
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

    const buildLegacyRedirectUrl = (code: string): string => {
        if (!code.trim()) {
            return '';
        }
        return `${API_BASE_URL}/groups/redirectInvite?code=${encodeURIComponent(code.trim())}`;
    };

    const handleGenerateInviteCode = async () => {
        await withActionGuard(async () => {
            const newInvite = await onGenerateInviteLink(gid);
            setLatestInviteCode(newInvite.code ?? '');
            setLatestLegacyRedirectLink(buildLegacyRedirectUrl(newInvite.code ?? ''));
            await loadInvites();
        });
    };

    const handleGenerateInviteLink = async () => {
        await withActionGuard(async () => {
            const newInvite = await onGenerateInviteLink(gid);
            setLatestInviteLink(newInvite.inviteUrl ?? '');
            setLatestLegacyRedirectLink(buildLegacyRedirectUrl(newInvite.code ?? ''));
            await loadInvites();
        });
    };

    const getInviteStatus = (invite: GroupInvitacionRespuesta): 'Activa' | 'Revocada' | 'Expirada' => {
        if (invite.revokedAt) return 'Revocada';
        if (invite.expiresAt && new Date(invite.expiresAt).getTime() < Date.now()) return 'Expirada';
        return 'Activa';
    };

    const loadInvites = useCallback(async () => {
        await withActionGuard(async () => {
            const list = await onLoadInvites(gid);
            setInvites(Array.isArray(list) ? list : []);
        });
    }, [gid, onLoadInvites, withActionGuard]);

    useEffect(() => {
        if (!isOpen || !canManageMembers) {
            return;
        }

        void loadInvites();
    }, [isOpen, canManageMembers, loadInvites]);

    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 font-['Space_Grotesk']">
            <div className="bg-[var(--astrais-panel-bg)] rounded-lg shadow-xl w-full max-w-2xl max-h-[90vh] overflow-hidden flex flex-col">
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
                                        onClick={() => void handleGenerateInviteCode()}
                                        disabled={!canManageMembers || isSubmittingAction}
                                        className="bg-accent-beige-300 text-black px-4 py-2 rounded-md font-medium hover:bg-accent-beige-400 transition-colors disabled:opacity-60"
                                    >
                                        Generar codigo
                                    </button>
                                    <button
                                        onClick={() => void handleGenerateInviteLink()}
                                        disabled={!canManageMembers || isSubmittingAction}
                                        className="bg-accent-beige-300 text-black px-4 py-2 rounded-md font-medium hover:bg-accent-beige-400 transition-colors disabled:opacity-60"
                                    >
                                        Generar enlace
                                    </button>
                                    <button
                                        onClick={() => void loadInvites()}
                                        disabled={!canManageMembers || isSubmittingAction}
                                        className="bg-gray-700 text-white px-4 py-2 rounded-md font-medium hover:bg-gray-600 transition-colors disabled:opacity-60"
                                    >
                                        Recargar invitaciones
                                    </button>
                                </div>
                            </div>

                            {(latestInviteCode || latestInviteLink || latestLegacyRedirectLink) && (
                                <div className="mt-3 grid grid-cols-1 gap-2 rounded-md border border-gray-700 bg-gray-900/60 p-3">
                                    <p className="text-sm text-gray-300">Ultima invitacion generada</p>
                                    {latestInviteCode ? (
                                        <input
                                            type="text"
                                            value={`Codigo: ${latestInviteCode}`}
                                            readOnly
                                            className="w-full bg-gray-800 border border-gray-700 rounded-md px-4 py-2 text-white"
                                        />
                                    ) : null}
                                    {latestInviteLink ? (
                                        <input
                                            type="text"
                                            value={latestInviteLink}
                                            readOnly
                                            className="w-full bg-gray-800 border border-gray-700 rounded-md px-4 py-2 text-white"
                                        />
                                    ) : null}
                                    {latestLegacyRedirectLink ? (
                                        <input
                                            type="text"
                                            value={latestLegacyRedirectLink}
                                            readOnly
                                            className="w-full bg-gray-800 border border-gray-700 rounded-md px-4 py-2 text-white"
                                        />
                                    ) : null}
                                </div>
                            )}

                            {canManageMembers && (
                                <div className="mt-3 max-h-56 overflow-y-auto rounded-md border border-gray-700 bg-gray-900/45 p-3">
                                    <p className="mb-2 text-sm font-semibold text-white">Invitaciones del grupo</p>
                                    {invites.length === 0 ? (
                                        <p className="text-sm text-gray-400">No hay invitaciones registradas.</p>
                                    ) : (
                                        <div className="space-y-2">
                                            {invites.map((invite) => {
                                                const status = getInviteStatus(invite);
                                                const canRevoke = status === 'Activa';

                                                return (
                                                    <div key={`${invite.code}-${invite.inviteUrl}`} className="rounded border border-gray-700 bg-gray-800/70 p-2">
                                                        <p className="text-sm text-white">Codigo: {invite.code}</p>
                                                        <p className="text-xs text-gray-300 break-all">{invite.inviteUrl}</p>
                                                        <p className="text-xs text-gray-400">
                                                            Estado: {status} · Usos: {invite.usesCount}/{invite.maxUses}
                                                        </p>
                                                        <p className="text-xs text-gray-400">
                                                            Expira: {invite.expiresAt ? new Date(invite.expiresAt).toLocaleString() : 'Sin caducidad'}
                                                        </p>
                                                        {invite.code ? (
                                                            <p className="text-xs text-gray-400 break-all">
                                                                Redireccion legado: {buildLegacyRedirectUrl(invite.code)}
                                                            </p>
                                                        ) : null}
                                                        {canRevoke ? (
                                                            <button
                                                                onClick={() => {
                                                                    void withActionGuard(async () => {
                                                                        await onRevokeInvite(gid, invite.code);
                                                                        await loadInvites();
                                                                    });
                                                                }}
                                                                disabled={isSubmittingAction}
                                                                className="mt-2 rounded bg-red-500/70 px-3 py-1 text-xs font-medium text-white hover:bg-red-500 disabled:opacity-60"
                                                            >
                                                                Revocar
                                                            </button>
                                                        ) : null}
                                                    </div>
                                                );
                                            })}
                                        </div>
                                    )}
                                </div>
                            )}

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
