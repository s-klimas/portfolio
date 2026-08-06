document.addEventListener('DOMContentLoaded', () => {
    document.querySelector('.skill-select-info').classList.add('active');
});

document.addEventListener('DOMContentLoaded', () => {
    const isMobile = window.matchMedia('(max-width: 800px)').matches;

    if (isMobile) {
        const skillsBox = document.getElementById('skills');

        if (skillsBox) {
            setTimeout(() => {
                skillsBox.scrollIntoView({
                    behavior: 'smooth',
                    block: 'start'
                });
            }, 150);
        }
    }
});

function hideChoose() {
    document.querySelector('.skill-select-info').style.display = 'none';
}

function hideAll() {
    document
        .querySelectorAll('.skill, .skill-select-info')
        .forEach(el => el.classList.remove('active'));
}

function deactivateAll() {
    document
        .querySelectorAll('.skill-point')
        .forEach(el => el.classList.remove('skill-point-active'));
}

function show(skillClass, pointClass) {
    hideChoose();
    hideAll();
    deactivateAll();

    document.querySelector(pointClass).classList.add('skill-point-active');
    document.querySelector(skillClass).classList.add('active');

    document.querySelector('.skill-info').classList.add('active');
}

function java() {
    show('.skill-java', '.skill-point-java');
}

function spring() {
    show('.skill-spring', '.skill-point-spring');
}

function orm() {
    show('.skill-orm', '.skill-point-orm');
}

function testing() {
    show('.skill-testing', '.skill-point-testing');
}

function javaTooling() {
    show('.skill-java-tooling', '.skill-point-java-tooling');
}

function mvc() {
    show('.skill-mvc', '.skill-point-mvc');
}

function desktop() {
    show('.skill-desktop', '.skill-point-desktop');
}

function postgresql() {
    show('.skill-postgresql', '.skill-point-postgresql');
}

function h2() {
    show('.skill-h2', '.skill-point-h2');
}

function mysql() {
    show('.skill-mysql', '.skill-point-mysql');
}

function liquibase() {
    show('.skill-liquibase', '.skill-point-liquibase');
}

function nosql() {
    show('.skill-nosql', '.skill-point-nosql');
}

function intellij() {
    show('.skill-intellij', '.skill-point-intellij');
}

function github() {
    show('.skill-github', '.skill-point-github');
}

function gitlab() {
    show('.skill-gitlab', '.skill-point-gitlab');
}

function docker() {
    show('.skill-docker', '.skill-point-docker');
}

function postman() {
    show('.skill-postman', '.skill-point-postman');
}

function frontend() {
    show('.skill-frontend', '.skill-point-frontend');
}

function figma() {
    show('.skill-figma', '.skill-point-figma');
}

function php() {
    show('.skill-php', '.skill-point-php');
}

function python() {
    show('.skill-python', '.skill-point-python');
}

document.querySelectorAll('.project').forEach(project => {
    project.addEventListener('click', () => {
        location.href = project.dataset.link;
    });
});

document.querySelectorAll('.project a').forEach(a => {
    a.addEventListener('click', (e) => {
        e.stopPropagation();
    });
});

// document.querySelectorAll('.project-full').forEach(project => {
//     project.addEventListener('click', () => {
//         location.href = project.dataset.link;
//     });
// });

document.querySelectorAll('.project-full a').forEach(a => {
    a.addEventListener('click', (e) => {
        e.stopPropagation();
    });
});

document.querySelectorAll('.skill-box').forEach(project => {
    project.addEventListener('click', () => {
        location.href = project.dataset.link;
    });
});

document.addEventListener('DOMContentLoaded', () => {
    const messagesContainer = document.getElementById('chatbotMessages');
    const input = document.getElementById('chatbotInput');
    const submitButton = document.getElementById('chatbotSubmit');

    const API_URL = '/streamedChat'; // ewentualnie '/chat' jeśli wolisz wersję niestreamowaną

    function appendMessage(text, cssClass) {
        const p = document.createElement('p');
        p.classList.add(cssClass);
        const a = document.createElement('a');
        a.textContent = text;
        p.appendChild(a);
        messagesContainer.appendChild(p);
        messagesContainer.scrollTop = messagesContainer.scrollHeight;
        return a; // zwracamy referencję do <a>, żeby móc ją aktualizować (streaming)
    }

    function setInputLocked(locked) {
        input.disabled = locked;
        submitButton.disabled = locked;
    }

    async function sendMessage() {
        const userText = input.value.trim();
        if (!userText) {
            return;
        }

        appendMessage(userText, 'chatbot-user-message');

        input.value = '';

        setInputLocked(true);

        const aiMessageEl = appendMessage('', 'chatbot-ai-massage');

        try {
            const response = await fetch(API_URL, {
                method: 'POST',
                headers: {
                    'Content-Type': 'text/plain'
                },
                body: userText
            });

            if (!response.ok) {
                throw new Error(`Server responded with status ${response.status}`);
            }

            if (!response.body) {
                const text = await response.text();
                aiMessageEl.textContent = text;
            } else {
                const reader = response.body.getReader();
                const decoder = new TextDecoder('utf-8');
                let fullText = '';

                while (true) {
                    const { done, value } = await reader.read();
                    if (done) {
                        break;
                    }
                    const chunk = decoder.decode(value, { stream: true });
                    fullText += chunk;
                    aiMessageEl.textContent = fullText;
                    messagesContainer.scrollTop = messagesContainer.scrollHeight;
                }
            }
        } catch (error) {
            console.error('Błąd podczas komunikacji z serwerem:', error);
            aiMessageEl.textContent = 'Wystąpił błąd podczas pobierania odpowiedzi.';
        } finally {
            setInputLocked(false);
            input.focus();
        }
    }

    submitButton.addEventListener('click', sendMessage);

    input.addEventListener('keydown', (event) => {
        if (event.key === 'Enter' && !submitButton.disabled) {
            event.preventDefault();
            sendMessage();
        }
    });
});