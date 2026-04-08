// components/modales/GroupSettingsModal.tsx
import { useEffect, useRef, useState, type ChangeEvent } from 'react';

interface GroupSettingsModalProps {
    isOpen: boolean;
    onClose: () => void;
    initialData: {
        name: string;
        description: string;
        members: Array<{ id: number; name: string; avatar?: string }>;
    };
    onSave: (data: {
        name: string;
        description: string;
        photo?: File | null;
        newMembers: string[];
    }) => void;
}

export default function GroupSettingsModal({
    isOpen,
    onClose,
    initialData,
    onSave
}: GroupSettingsModalProps) {
    const [name, setName] = useState<string>(initialData.name);
    const [description, setDescription] = useState<string>(initialData.description);
    const [photo, setPhoto] = useState<File | null>(null);
    const [newMemberInput, setNewMemberInput] = useState<string>('');
    const [newMembers, setNewMembers] = useState<string[]>([]);
    const [previewUrl, setPreviewUrl] = useState<string | null>(null);
    const fileInputRef = useRef<HTMLInputElement>(null);

    useEffect(() => {
        if (!isOpen) return;

        setName(initialData.name);
        setDescription(initialData.description);
        setPhoto(null);
        setNewMemberInput('');
        setNewMembers([]);
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

    const addNewMember = () => {
        if (newMemberInput.trim() && !newMembers.includes(newMemberInput.trim())) {
            setNewMembers([...newMembers, newMemberInput.trim()]);
            setNewMemberInput('');
        }
    };

    const removeNewMember = (member: string) => {
        setNewMembers(newMembers.filter(m => m !== member));
    };

    const handleSubmit = () => {
        onSave({
            name,
            description,
            photo,
            newMembers
        });
    };

    const triggerFileInput = () => {
        fileInputRef.current?.click();
    };

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 font-['Space_Grotesk']">
            <div className="bg-[linear-gradient(160deg,#0a101ff2,#3c1480d9,#142f42e6)] rounded-lg shadow-xl w-full max-w-2xl max-h-[90vh] overflow-hidden flex flex-col">
                <div className="overflow-y-auto grow p-6">
                    <div className="flex justify-between items-center mb-4">
                        <h2 className="text-2xl font-bold text-white">Configuración del Grupo</h2>
                        <button
                            onClick={onClose}
                            className="text-gray-400 hover:text-white text-2xl"
                        >
                            &times;
                        </button>
                    </div>

                    <div className="space-y-6">
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
                            <label className="block text-gray-300 mb-2">Descripción</label>
                            <textarea
                                value={description}
                                onChange={(e) => setDescription(e.target.value)}
                                rows={3}
                                className="w-full bg-gray-800 border border-gray-700 rounded-md px-4 py-2 text-white focus:outline-none focus:ring-2 focus:ring-accent-beige-300"
                            />
                        </div>

                        <div>
                            <h3 className="text-lg font-semibold text-white mb-2">Miembros actuales</h3>
                            <div className="space-y-2 max-h-40 overflow-y-auto pr-2">
                                {initialData.members.map((member) => (
                                    <div key={member.id} className="flex items-center gap-3 p-2 bg-gray-800 rounded">
                                        <div className="w-8 h-8 rounded-full bg-gray-600 flex items-center justify-center">
                                            <span className="text-xs">{member.name.charAt(0)}</span>
                                        </div>
                                        <span className="text-white">{member.name}</span>
                                    </div>
                                ))}
                            </div>
                        </div>

                        <div>
                            <h3 className="text-lg font-semibold text-white mb-2">Añadir nuevos miembros</h3>
                            <div className="flex gap-2">
                                <input
                                    type="text"
                                    value={newMemberInput}
                                    onChange={(e) => setNewMemberInput(e.target.value)}
                                    placeholder="Email o nombre"
                                    className="grow bg-gray-800 border border-gray-700 rounded-md px-4 py-2 text-white focus:outline-none focus:ring-2 focus:ring-accent-beige-300"
                                    onKeyDown={(e) => e.key === 'Enter' && (e.preventDefault(), addNewMember())}
                                />
                                <button
                                    onClick={addNewMember}
                                    className="bg-accent-beige-300 text-black px-4 py-2 rounded-md font-medium hover:bg-accent-beige-400 transition-colors"
                                >
                                    Añadir
                                </button>
                            </div>

                            {newMembers.length > 0 && (
                                <div className="mt-3 space-y-1">
                                    {newMembers.map((member, index) => (
                                        <div key={index} className="flex justify-between items-center p-2 bg-gray-800 rounded">
                                            <span className="text-white">{member}</span>
                                            <button
                                                onClick={() => removeNewMember(member)}
                                                className="text-red-500 hover:text-red-400"
                                            >
                                                ×
                                            </button>
                                        </div>
                                    ))}
                                </div>
                            )}
                        </div>
                    </div>
                </div>

                <div className="border-t border-gray-700 p-4 flex justify-end gap-3">
                    <button
                        onClick={onClose}
                        className="px-6 py-2 border border-gray-600 rounded-md text-white hover:bg-gray-700 transition-colors"
                    >
                        Cancelar
                    </button>
                    <button
                        onClick={handleSubmit}
                        className="px-6 py-2 bg-accent-beige-300 text-black rounded-md font-medium hover:bg-accent-beige-400 transition-colors"
                    >
                        Guardar cambios
                    </button>
                </div>
            </div>
        </div>
    );
}
