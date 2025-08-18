/* 사용자가 만든 페이지 불러오기 */
import Router from '../routes';

/* 최상위 페이지 */
const App = () => {
    return (
        /*
            사용자가 만든 페이지 출력
                * 윈도우의 경우 Ctrl을 누르신 뒤 App을 클릭하시면 해당 파일로 이동하실 수 있습니다.
                * 맥의 경우 Command를 누르신 뒤 App을 클릭하시면 해당 파일로 이동하실 수 있습니다.
        */
        <Router />
    )
}

/* App/index.js에서 import하기 위한 설정 */
export default App;