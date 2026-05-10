// Modal de creacion de grupos.
import { useState } from 'react';

interface CreateGroupFormData {
    name: string;
    description: string;
    photo?: File | null;
}

interface CreateGroupModalProps {
    isOpen: boolean;
    onClose: () => void;
    onSave: (data: CreateGroupFormData) => void | Promise<void>;
}

export default function CreateGroupModal({
    isOpen,
    onClose,
    onSave
}: CreateGroupModalProps) {
    const [name, setName] = useState<string>("");
    const [description, setDescription] = useState<string>("");
    const [photo, setPhoto] = useState<File | null>(null);


    if (!isOpen) return null;


    const handleSubmit = () => {
        // Envia solo los campos editables y despues limpia el formulario.
        onSave({
            name: name,
            description: description,
            photo
        });
        cleanModal();
    };

    const handleClose = () => {
        // Al cancelar se descartan los datos locales para abrir limpio despues.
        cleanModal();
        onClose();
    }

    const cleanModal = () => {
        setName("");
        setDescription("");
        setPhoto(null);
    }

    return (
        <div className="astrais-modal-overlay fixed inset-0 z-50 flex items-center justify-center p-4 font-['Space_Grotesk']">
            <div className="astrais-modal-surface flex w-full max-w-2xl flex-col overflow-hidden rounded-lg max-h-[90vh]">
                <div className="overflow-y-auto grow p-6">
                    {/* Cabecera del modal de creacion */}
                    <div className="flex justify-between items-center mb-4">
                        <h2 className="text-2xl font-bold text-white">Crear grupo</h2>
                        <button
                            onClick={onClose}
                            className="text-white/60 hover:text-white text-2xl"
                        >
                            &times;
                        </button>
                    </div>

                    <div className="space-y-6">

                        {/* Campos principales del grupo */}
                        <div>
                            <label className="block text-white/75 mb-2">Nombre del grupo</label>
                            <input
                                type="text"
                                value={name}
                                onChange={(e) => setName(e.target.value)}
                                className="astrais-modal-control w-full rounded-md px-4 py-2"
                            />
                        </div>

                        <div>
                            <label className="block text-white/75 mb-2">Descripción</label>
                            <textarea
                                value={description}
                                onChange={(e) => setDescription(e.target.value)}
                                rows={3}
                                className="astrais-modal-control w-full rounded-md px-4 py-2"
                            />
                        </div>

                    </div>
                </div>

                {/* Acciones de guardado o cancelacion */}
                <div className="border-t border-white/10 p-4 flex justify-end gap-3">
                    <button
                        onClick={handleClose}
                        className="px-6 py-2 border border-white/15 rounded-md text-white hover:bg-white/10 transition-colors"
                    >
                        Cancelar
                    </button>
                    <button
                        onClick={handleSubmit}
                        className="px-6 py-2 rounded-md font-medium text-white [background:var(--astrais-cta-bg)] transition-colors hover:brightness-110"
                    >
                        Guardar cambios
                    </button>
                </div>
            </div>
        </div>
    );
}
