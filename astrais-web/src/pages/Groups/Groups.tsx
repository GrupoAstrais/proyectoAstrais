import Navbar from "../../components/layout/Navbar"
import bgImage from '../../assets/homeScreenBack.jpg'
import GroupCard from "../../components/ui/GroupCard"
import GroupModal from "../../components/modales/GroupModal"
import React, { useState } from "react";

export default function Groups() {
    const [isOpen, setIsOpen] = React.useState<boolean>(false);

    const [activeGroup, setActiveGroup] = useState<number>(-1);
    
    const handleActiveGroup = (active: number) => {
        if(isOpen == false || active != activeGroup) {
          setIsOpen(true);  
          setActiveGroup(active);
        } else {
            setIsOpen(false);
            setActiveGroup(-1);
        }
    }

    return (
        <div style={{ backgroundImage: `url(${bgImage})` }} className="flex flex-col gap-4 relative min-h-screen bg-cover bg-center font-['Space_Grotesk'] text-white">
            <Navbar /> 
            <div className="md:flex md:flex-row justify-center px-5 grid grid-cols-1 gap-2 ">
                <div className="w-1/3 flex flex-col gap-2">
                    <GroupCard onClick={handleActiveGroup} id={0} activeId={activeGroup} />
                    <GroupCard onClick={handleActiveGroup} id={1} activeId={activeGroup} />
                    <GroupCard onClick={handleActiveGroup} id={2} activeId={activeGroup} />
                </div>
                <div className={`${isOpen ? '' : 'hidden'} w-1/2`}>
                    <GroupModal />
                </div>
            </div>
        </div>
    )
}